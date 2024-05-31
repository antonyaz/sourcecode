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

package org.apache.flink.runtime.heartbeat;

import org.apache.flink.runtime.clusterframework.types.ResourceID;
import org.apache.flink.runtime.concurrent.ScheduledExecutor;

import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * {@link HeartbeatManager} implementation which regularly requests a heartbeat response from its
 * monitored {@link HeartbeatTarget}. The heartbeat period is configurable.
 *
 * @param <I> Type of the incoming heartbeat payload
 * @param <O> Type of the outgoing heartbeat payload
 */
public class HeartbeatManagerSenderImpl<I, O> extends HeartbeatManagerImpl<I, O>
        implements Runnable {

    private final long heartbeatPeriod;

    HeartbeatManagerSenderImpl(
            long heartbeatPeriod,
            long heartbeatTimeout,
            ResourceID ownResourceID,
            HeartbeatListener<I, O> heartbeatListener,
            ScheduledExecutor mainThreadExecutor,
            Logger log) {
        this(
                heartbeatPeriod,
                heartbeatTimeout,
                ownResourceID,
                heartbeatListener,
                mainThreadExecutor,
                log,
                new HeartbeatMonitorImpl.Factory<>());
    }

    HeartbeatManagerSenderImpl(
            long heartbeatPeriod,
            long heartbeatTimeout,
            ResourceID ownResourceID,
            HeartbeatListener<I, O> heartbeatListener,
            ScheduledExecutor mainThreadExecutor,
            Logger log,
            HeartbeatMonitor.Factory<O> heartbeatMonitorFactory) {
        super(
                heartbeatTimeout,
                ownResourceID,
                heartbeatListener,
                mainThreadExecutor,
                log,
                heartbeatMonitorFactory);

        /**
         * TODO: add by antony at 2022/5/2
         * 默认10s
         */
        this.heartbeatPeriod = heartbeatPeriod;
        /**
         * TODO: add by antony at 2022/5/2
         * 启动调度任务运行一次
         */
        mainThreadExecutor.schedule(this, 0L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        if (!stopped) {
            log.debug("Trigger heartbeat request.");
            /**
             * TODO: add by antony at 2022/5/2
             * 发送心跳
             * HeartbeatMonitor： 心跳监视器
             * HeartbeatTarget： 心跳目标对象 与  HeartbeatMonitor 是一一对应关系
             * 以ResourceManager 和 TaskExecutor 这个主从关系来讲
             * ResourceManager内部有一个HeartBeatManagerSenderImpl 组件
             * TaskExecutor 将来将ResourceManager执行注册成功之后，会被封装为一个HeartbeatTareget加入到HeartbeatManagerSenderImpl中
             */
            for (HeartbeatMonitor<O> heartbeatMonitor : getHeartbeatTargets().values()) {
                /**
                 * TODO: add by antony at 2022/5/2
                 * 给心跳目标对象发送心跳
                 * 主节点给所有从节点发送
                 */
                requestHeartbeat(heartbeatMonitor);
            }

            /**
             * TODO: add by antony at 2022/5/2
             * 启动定时任务
             * 每隔heartbeatPeriod 定时调度心跳服务
             */
            getMainThreadExecutor().schedule(this, heartbeatPeriod, TimeUnit.MILLISECONDS);

            /**
             * TODO: add by antony at 2022/5/2
             * 结论：
             * 1、主节点 主动给 从角色发送心跳，每隔10s
             * 2、从角色接收到心跳RPC请求之后，会记录最近一次的心跳时间
             * 同时给主角色返回一个响应，然后主角色也会记录该时间
             */
        }
    }

    private void requestHeartbeat(HeartbeatMonitor<O> heartbeatMonitor) {
        O payload = getHeartbeatListener().retrievePayload(heartbeatMonitor.getHeartbeatTargetId());
        final HeartbeatTarget<O> heartbeatTarget = heartbeatMonitor.getHeartbeatTarget();

        heartbeatTarget.requestHeartbeat(getOwnResourceID(), payload);
    }
}
