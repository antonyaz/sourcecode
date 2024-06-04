/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.client.deployment.executors;

import org.apache.flink.annotation.Internal;
import org.apache.flink.api.dag.Pipeline;
import org.apache.flink.client.ClientUtils;
import org.apache.flink.client.deployment.ClusterClientFactory;
import org.apache.flink.client.deployment.ClusterClientJobClientAdapter;
import org.apache.flink.client.deployment.ClusterDescriptor;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.ClusterClientProvider;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.execution.JobClient;
import org.apache.flink.core.execution.PipelineExecutor;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.util.function.FunctionUtils;

import javax.annotation.Nonnull;

import java.util.concurrent.CompletableFuture;

import static org.apache.flink.util.Preconditions.checkNotNull;
import static org.apache.flink.util.Preconditions.checkState;

/**
 * An abstract {@link PipelineExecutor} used to execute {@link Pipeline pipelines} on an existing
 * (session) cluster.
 *
 * @param <ClusterID> the type of the id of the cluster.
 * @param <ClientFactory> the type of the {@link ClusterClientFactory} used to create/retrieve a
 *     client to the target cluster.
 */
@Internal
public class AbstractSessionClusterExecutor<
                ClusterID, ClientFactory extends ClusterClientFactory<ClusterID>>
        implements PipelineExecutor {

    private final ClientFactory clusterClientFactory;

    public AbstractSessionClusterExecutor(@Nonnull final ClientFactory clusterClientFactory) {
        this.clusterClientFactory = checkNotNull(clusterClientFactory);
    }

    /**
     * todo: pipeline即传入的streamGraph
     */
    @Override
    public CompletableFuture<JobClient> execute(
            @Nonnull final Pipeline pipeline,
            @Nonnull final Configuration configuration,
            @Nonnull final ClassLoader userCodeClassloader)
            throws Exception {
        /**
         *  todo: add by antony at 2022/5/3
         *  将StreamGraph转换成JobGraph
         *  pipeline 即 StreamGraph
         *  1、将 StreamGraph 转换为 JobGraph
         *  2、设置 JobGraph 中的 JobVertex 能否 chain
         *  3、设置 checkpoint 配置信息
         *  4、设置 JobGraph 中依赖的资源文件
         *
         */
        final JobGraph jobGraph = PipelineExecutorUtils.getJobGraph(pipeline, configuration);

        /**
         * todo: 关于提交Job的流程，是一种简单的C/S架构实现
         * 1、客户端RestclusterClient: 启动一个Netty客户端
         * 2、 服务器WebMonitorEndpoint： 启动一个Netty服务器
         * 1 与 2 通信方式： rest请求
         * 客户端提交了JobGraph 给主节点，主节点中的WebMonitorEndpoint 中的某一个Handler来执行这个url请求的处理
         *
         * 注意事项：
         * 1、不是直接把JobGraph传送给WebMonitorEndpoint
         * 2、而是先把JobGraph序列化到一个本地磁盘文件，然后这个JobGraphFile连同Jar包等传输给WebMonitorEndpoint
         *
         */
        try (final ClusterDescriptor<ClusterID> clusterDescriptor =
                clusterClientFactory.createClusterDescriptor(configuration)) {
            final ClusterID clusterID = clusterClientFactory.getClusterId(configuration);
            checkState(clusterID != null);

            /**
             * todo: 构建clusterClient = RestClusterClient
             * 在这个东西的内部，启动一个nettty的客户端
             * RestClusterClient
             */
            final ClusterClientProvider<ClusterID> clusterClientProvider =
                    clusterDescriptor.retrieve(clusterID);
            ClusterClient<ClusterID> clusterClient = clusterClientProvider.getClusterClient();
            /**
             * todo: 提交JobGraph到WebMonitorEndpoint
             */
            return clusterClient
                    .submitJob(jobGraph) //提交Job
                    .thenApplyAsync(
                            FunctionUtils.uncheckedFunction(
                                    jobId -> {
                                        ClientUtils.waitUntilJobInitializationFinished(
                                                () -> clusterClient.getJobStatus(jobId).get(),
                                                () -> clusterClient.requestJobResult(jobId).get(),
                                                userCodeClassloader);
                                        return jobId;
                                    }))
                    .thenApplyAsync(
                            jobID ->
                                    (JobClient)
                                            new ClusterClientJobClientAdapter<>(
                                                    clusterClientProvider,
                                                    jobID,
                                                    userCodeClassloader))
                    .whenCompleteAsync((ignored1, ignored2) -> clusterClient.close());
        }
    }
}
