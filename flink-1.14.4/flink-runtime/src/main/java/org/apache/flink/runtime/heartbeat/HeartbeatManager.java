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

/**
 * A heartbeat manager has to be able to start/stop monitoring a {@link HeartbeatTarget}, and report
 * heartbeat timeouts for this target.
 *
 * @param <I> Type of the incoming payload
 * @param <O> Type of the outgoing payload
 */
/**
  todo: add by antony at: 2023/7/16
  心跳管理者，用于start/stop监控对象
*/
public interface HeartbeatManager<I, O> extends HeartbeatTarget<I> {

    /**
     * Start monitoring a {@link HeartbeatTarget}. Heartbeat timeouts for this target are reported
     * to the {@link HeartbeatListener} associated with this heartbeat manager.
     *
     * @param resourceID Resource ID identifying the heartbeat target
     * @param heartbeatTarget Interface to send heartbeat requests and responses to the heartbeat
     *     target
     */
    /**
      todo: add by antony at: 2023/7/16
     开始监控一个目标，
    */
    void monitorTarget(ResourceID resourceID, HeartbeatTarget<O> heartbeatTarget);

    /**
     * Stops monitoring the heartbeat target with the associated resource ID.
     *
     * @param resourceID Resource ID of the heartbeat target which shall no longer be monitored
     */
    /**
      todo: add by antony at: 2023/7/16
      停止监控
    */
    void unmonitorTarget(ResourceID resourceID);

    /** Stops the heartbeat manager. */
    /**
      todo: add by antony at: 2023/7/16
      停止管理
    */
    void stop();

    /**
     * Returns the last received heartbeat from the given target.
     *
     * @param resourceId for which to return the last heartbeat
     * @return Last heartbeat received from the given target or -1 if the target is not being
     *     monitored.
     */
    /**
      todo: add by antony at: 2023/7/16
     获取最后一次心跳时间
    */
    long getLastHeartbeatFrom(ResourceID resourceId);
}
