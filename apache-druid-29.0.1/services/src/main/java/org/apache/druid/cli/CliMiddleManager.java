/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.druid.cli;

import com.github.rvesse.airline.annotations.Command;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import org.apache.druid.curator.ZkEnablementConfig;
import org.apache.druid.discovery.NodeRole;
import org.apache.druid.discovery.WorkerNodeService;
import org.apache.druid.guice.IndexingServiceFirehoseModule;
import org.apache.druid.guice.IndexingServiceInputSourceModule;
import org.apache.druid.guice.IndexingServiceModuleHelper;
import org.apache.druid.guice.IndexingServiceTaskLogsModule;
import org.apache.druid.guice.IndexingServiceTuningConfigModule;
import org.apache.druid.guice.Jerseys;
import org.apache.druid.guice.JsonConfigProvider;
import org.apache.druid.guice.LazySingleton;
import org.apache.druid.guice.LifecycleModule;
import org.apache.druid.guice.ManageLifecycle;
import org.apache.druid.guice.MiddleManagerServiceModule;
import org.apache.druid.guice.PolyBind;
import org.apache.druid.guice.annotations.Self;
import org.apache.druid.indexing.common.RetryPolicyFactory;
import org.apache.druid.indexing.common.TaskStorageDirTracker;
import org.apache.druid.indexing.common.config.TaskConfig;
import org.apache.druid.indexing.common.stats.DropwizardRowIngestionMetersFactory;
import org.apache.druid.indexing.common.task.batch.parallel.ParallelIndexSupervisorTaskClientProvider;
import org.apache.druid.indexing.common.task.batch.parallel.ShuffleClient;
import org.apache.druid.indexing.overlord.ForkingTaskRunner;
import org.apache.druid.indexing.overlord.TaskRunner;
import org.apache.druid.indexing.worker.Worker;
import org.apache.druid.indexing.worker.WorkerCuratorCoordinator;
import org.apache.druid.indexing.worker.WorkerTaskManager;
import org.apache.druid.indexing.worker.WorkerTaskMonitor;
import org.apache.druid.indexing.worker.config.WorkerConfig;
import org.apache.druid.indexing.worker.http.TaskManagementResource;
import org.apache.druid.indexing.worker.http.WorkerResource;
import org.apache.druid.indexing.worker.shuffle.DeepStorageIntermediaryDataManager;
import org.apache.druid.indexing.worker.shuffle.IntermediaryDataManager;
import org.apache.druid.indexing.worker.shuffle.LocalIntermediaryDataManager;
import org.apache.druid.indexing.worker.shuffle.ShuffleModule;
import org.apache.druid.java.util.common.logger.Logger;
import org.apache.druid.metadata.input.InputSourceModule;
import org.apache.druid.query.DruidMetrics;
import org.apache.druid.query.lookup.LookupSerdeModule;
import org.apache.druid.segment.incremental.RowIngestionMetersFactory;
import org.apache.druid.segment.realtime.appenderator.AppenderatorsManager;
import org.apache.druid.segment.realtime.appenderator.DummyForInjectionAppenderatorsManager;
import org.apache.druid.segment.realtime.firehose.ChatHandlerProvider;
import org.apache.druid.segment.realtime.firehose.NoopChatHandlerProvider;
import org.apache.druid.server.DruidNode;
import org.apache.druid.server.http.SelfDiscoveryResource;
import org.apache.druid.server.initialization.jetty.JettyServerInitializer;
import org.apache.druid.server.metrics.ServiceStatusMonitor;
import org.apache.druid.server.metrics.WorkerTaskCountStatsProvider;
import org.apache.druid.timeline.PruneLastCompactionState;
import org.eclipse.jetty.server.Server;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 */
@Command(
    name = "middleManager",
    description = "Runs a Middle Manager, this is a \"task\" node used as part of the remote indexing service, see https://druid.apache.org/docs/latest/design/middlemanager.html for a description"
)
/**
  todo: add by antony at: 2024/5/31
  主要负责用于数据的接入
*/
public class CliMiddleManager extends ServerRunnable
{
  private static final Logger log = new Logger(CliMiddleManager.class);

  private boolean isZkEnabled = true;

  public CliMiddleManager()
  {
    super(log);
  }

  @Inject
  public void configure(Properties properties)
  {
    /**
      todo: add by antony at: 2024/5/31
      是否已开启zk
    */
    isZkEnabled = ZkEnablementConfig.isEnabled(properties);
  }

  /**
    todo: add by antony at: 2024/5/31
    该组件的角色仅为 MidlleManager
  */
  @Override
  protected Set<NodeRole> getNodeRoles(Properties properties)
  {
    return ImmutableSet.of(NodeRole.MIDDLE_MANAGER);
  }

  @Override
  protected List<? extends Module> getModules()
  {
    return ImmutableList.of(
            /**
              todo: add by antony at: 2024/5/31
              组件基本信息
            */
        new MiddleManagerServiceModule(),
        new Module()
        {
          @Override
          public void configure(Binder binder)
          {
            /**
              todo: add by antony at: 2024/5/31
              绑定组件信息
            */
            binder.bindConstant().annotatedWith(Names.named("serviceName")).to("druid/middlemanager");
            binder.bindConstant().annotatedWith(Names.named("servicePort")).to(8091);
            binder.bindConstant().annotatedWith(Names.named("tlsServicePort")).to(8291);
            binder.bindConstant().annotatedWith(PruneLastCompactionState.class).to(true);

            /**
              todo: add by antony at: 2024/5/31
              配置数据拉取任务的RunnerConfig
            */
            IndexingServiceModuleHelper.configureTaskRunnerConfigs(binder);

            JsonConfigProvider.bind(binder, "druid.indexer.task", TaskConfig.class);
            JsonConfigProvider.bind(binder, "druid.worker", WorkerConfig.class);
            binder.bind(RetryPolicyFactory.class).in(LazySingleton.class);

            /**
              todo: add by antony at: 2024/5/31
              绑定 ForkingTaskRunner， 核心点
             以进程的方式来执行
             1、获取执行task所需要的槽位
             2、提交该task及对应的task的statusFuture
             3、创建该task对应的文件夹及文件
             4、组装运行该task所需要的command 命令，包含了 add-exports、javaopts、系统环境变量、属性对象、jvm参数、指定的参数、任务运行参数等来启动 internal peon
             5、等待task执行结果
             6、执行task完毕后，事后处理
             7、保存运行中的任务列表
            */
            binder.bind(TaskRunner.class).to(ForkingTaskRunner.class);
            binder.bind(ForkingTaskRunner.class).in(ManageLifecycle.class);
            binder.bind(WorkerTaskCountStatsProvider.class).to(ForkingTaskRunner.class);

            binder.bind(ParallelIndexSupervisorTaskClientProvider.class).toProvider(Providers.of(null));
            /**
              todo: add by antony at: 2024/5/31
              执行并行索引时 intermediate data 执行 shuffle的客户端
            */
            binder.bind(ShuffleClient.class).toProvider(Providers.of(null));
            binder.bind(ChatHandlerProvider.class).toProvider(Providers.of(new NoopChatHandlerProvider()));
            PolyBind.createChoice(
                binder,
                "druid.indexer.task.rowIngestionMeters.type",
                Key.get(RowIngestionMetersFactory.class),
                Key.get(DropwizardRowIngestionMetersFactory.class)
            );
            final MapBinder<String, RowIngestionMetersFactory> rowIngestionMetersHandlerProviderBinder =
                PolyBind.optionBinder(binder, Key.get(RowIngestionMetersFactory.class));
            rowIngestionMetersHandlerProviderBinder
                .addBinding("dropwizard")
                .to(DropwizardRowIngestionMetersFactory.class)
                .in(LazySingleton.class);
            binder.bind(DropwizardRowIngestionMetersFactory.class).in(LazySingleton.class);

            /**
              todo: add by antony at: 2024/5/31
              binder中来安装相关 workerMgr 组件
            */
            binder.install(makeWorkerManagementModule(isZkEnabled));

            /**
              todo: add by antony at: 2024/5/31
              binder中来绑定 jetty 的 web接口
            */
            binder.bind(JettyServerInitializer.class)
                  .to(MiddleManagerJettyServerInitializer.class)
                  .in(LazySingleton.class);

            /**
              todo: add by antony at: 2024/5/31
              用来创建及管理 Appenderator 对象
             AppenderatorsManager 主要
             1、用于当运行在 peon中及 CliIndexer进程中的任务在需要Appenderator时
             2、用于创建需要在读取Appenderator中所包含的数据的 QueryRunner实例时
             这个对象中的方法可多线程来调用
            */
            binder.bind(AppenderatorsManager.class)
                  .to(DummyForInjectionAppenderatorsManager.class)
                  .in(LazySingleton.class);

            /**
              todo: add by antony at: 2024/5/31
              启动jetty web server
            */
            LifecycleModule.register(binder, Server.class);

            bindAnnouncer(
                binder,
                DiscoverySideEffectsProvider.create()
            );

            Jerseys.addResource(binder, SelfDiscoveryResource.class);
            LifecycleModule.registerKey(binder, Key.get(SelfDiscoveryResource.class));

            configureIntermediaryData(binder);
          }

          private void configureIntermediaryData(Binder binder)
          {
            PolyBind.createChoice(
                binder,
                "druid.processing.intermediaryData.storage.type",
                Key.get(IntermediaryDataManager.class),
                Key.get(LocalIntermediaryDataManager.class)
            );
            final MapBinder<String, IntermediaryDataManager> biddy = PolyBind.optionBinder(
                binder,
                Key.get(IntermediaryDataManager.class)
            );
            biddy.addBinding("local").to(LocalIntermediaryDataManager.class);
            biddy.addBinding("deepstore").to(DeepStorageIntermediaryDataManager.class).in(LazySingleton.class);
          }

          @Provides
          @LazySingleton
          @Named(ServiceStatusMonitor.HEARTBEAT_TAGS_BINDING)
          public Supplier<Map<String, Object>> heartbeatDimensions(WorkerConfig workerConfig, WorkerTaskManager workerTaskManager)
          {
            return () -> ImmutableMap.of(
                DruidMetrics.WORKER_VERSION, workerConfig.getVersion(),
                DruidMetrics.CATEGORY, workerConfig.getCategory(),
                DruidMetrics.STATUS, workerTaskManager.isWorkerEnabled() ? "Enabled" : "Disabled"
            );
          }

          @Provides
          @LazySingleton
          public Worker getWorker(@Self DruidNode node, WorkerConfig config)
          {
            return new Worker(
                node.getServiceScheme(),
                node.getHostAndPortToUse(),
                config.getIp(),
                config.getCapacity(),
                config.getVersion(),
                config.getCategory()
            );
          }

          @Provides
          @LazySingleton
          public WorkerNodeService getWorkerNodeService(WorkerConfig workerConfig)
          {
            return new WorkerNodeService(
                workerConfig.getIp(),
                workerConfig.getCapacity(),
                workerConfig.getVersion(),
                workerConfig.getCategory()
            );
          }
        },
        new ShuffleModule(),
        new IndexingServiceFirehoseModule(),
        new IndexingServiceInputSourceModule(),
            /**
              todo: add by antony at: 2024/5/31
              indexingServiceTask的日志模块
            */
        new IndexingServiceTaskLogsModule(),
            /**
              todo: add by antony at: 2024/5/31
              indexingService的调优模块
            */
        new IndexingServiceTuningConfigModule(),
        new InputSourceModule(),
        new LookupSerdeModule()
    );
  }

  public static Module makeWorkerManagementModule(boolean isZkEnabled)
  {
    return new Module()
    {
      @Override
      public void configure(Binder binder)
      {
        if (isZkEnabled) {
          binder.bind(WorkerTaskManager.class).to(WorkerTaskMonitor.class);
          binder.bind(WorkerTaskMonitor.class).in(ManageLifecycle.class);
          binder.bind(WorkerCuratorCoordinator.class).in(ManageLifecycle.class);
          LifecycleModule.register(binder, WorkerTaskMonitor.class);
        } else {
          binder.bind(WorkerTaskManager.class).in(ManageLifecycle.class);
        }

        Jerseys.addResource(binder, WorkerResource.class);
        Jerseys.addResource(binder, TaskManagementResource.class);
      }

      @Provides
      @ManageLifecycle
      public TaskStorageDirTracker getTaskStorageDirTracker(WorkerConfig workerConfig, TaskConfig taskConfig)
      {
        return TaskStorageDirTracker.fromConfigs(workerConfig, taskConfig);
      }
    };
  }
}
