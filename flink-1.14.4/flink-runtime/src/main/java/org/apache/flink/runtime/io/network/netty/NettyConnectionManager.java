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

package org.apache.flink.runtime.io.network.netty;

import org.apache.flink.runtime.io.network.ConnectionID;
import org.apache.flink.runtime.io.network.ConnectionManager;
import org.apache.flink.runtime.io.network.PartitionRequestClient;
import org.apache.flink.runtime.io.network.TaskEventPublisher;
import org.apache.flink.runtime.io.network.partition.ResultPartitionProvider;

import java.io.IOException;

import static org.apache.flink.util.Preconditions.checkNotNull;

public class NettyConnectionManager implements ConnectionManager {

    private final NettyServer server;

    private final NettyClient client;

    /**
     * todo: add by antony at 2022/5/3
     * netty server 和 netty client 之间的通信协议
     */
    private final NettyBufferPool bufferPool;

    private final PartitionRequestClientFactory partitionRequestClientFactory;

    private final NettyProtocol nettyProtocol;

    public NettyConnectionManager(
            ResultPartitionProvider partitionProvider,
            TaskEventPublisher taskEventPublisher,
            NettyConfig nettyConfig) {

        /**
         *  todo: add by antony at 2022/5/3
         *  初始化 netty server
         */
        this.server = new NettyServer(nettyConfig);

        /**
         *  todo: add by antony at 2022/5/3
         *  初始化 netty client
         */
        this.client = new NettyClient(nettyConfig);
        this.bufferPool = new NettyBufferPool(nettyConfig.getNumberOfArenas());

        /**
         *  todo: add by antony at 2022/5/3
         *  初始化 一个 包装类
         */
        this.partitionRequestClientFactory =
                new PartitionRequestClientFactory(client, nettyConfig.getNetworkRetries());

        /**
         *  todo: add by antony at 2022/5/3
         *  netty client 和 netty server 之间的通信协议
         *  这俩在运行的时候，都需要去绑定一些 Handler来完成请求处理
         *  so，通信协议提供了两个方法
         *  1、提供NettyServer所需要的Handler： getServerChannelHandlers
         *  2、提供NettyClient所需要的Handler： getClientChannelHandlers
         */
        this.nettyProtocol =
                new NettyProtocol(
                        checkNotNull(partitionProvider), checkNotNull(taskEventPublisher));
    }

    /**
     * todo: add by antony at 2022/5/3
     * 启动 client 和 server
     */
    @Override
    public int start() throws IOException {
        /**
         *  todo: add by antony at 2022/5/3
         *  netty的客户端启动
         *  client = Netty Client
         */
        client.init(nettyProtocol, bufferPool);

        /**
         *  todo: add by antony at 2022/5/3
         *  netty的服务端启动
         *  server = Netty Server
         */
        return server.init(nettyProtocol, bufferPool);
    }

    @Override
    public PartitionRequestClient createPartitionRequestClient(ConnectionID connectionId)
            throws IOException, InterruptedException {
        return partitionRequestClientFactory.createPartitionRequestClient(connectionId);
    }

    @Override
    public void closeOpenChannelConnections(ConnectionID connectionId) {
        partitionRequestClientFactory.closeOpenChannelConnections(connectionId);
    }

    @Override
    public int getNumberOfActiveConnections() {
        return partitionRequestClientFactory.getNumberOfActiveClients();
    }

    @Override
    public void shutdown() {
        client.shutdown();
        server.shutdown();
    }

    NettyClient getClient() {
        return client;
    }

    NettyServer getServer() {
        return server;
    }

    NettyBufferPool getBufferPool() {
        return bufferPool;
    }
}
