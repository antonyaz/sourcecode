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
 * Interface for the interaction with the {@link HeartbeatManager}. The heartbeat listener is used
 * for the following things:
 *
 * <ul>
 *   <li>Notifications about heartbeat timeouts
 *   <li>Payload reports of incoming heartbeats
 *   <li>Retrieval of payloads for outgoing heartbeats
 * </ul>
 *
 * @param <I> Type of the incoming payload
 * @param <O> Type of the outgoing payload
 */
/**
  todo: add by antony at: 2023/7/16
  与 HeartbeatManager 交互的接口，用于以下事情：
  1、关于心跳超时的通知
 2、输入心跳的payload
 3、提取出心跳的payload
*/
public interface HeartbeatListener<I, O> {

    /**
     * Callback which is called if a heartbeat for the machine identified by the given resource ID
     * times out.
     *
     * @param resourceID Resource ID of the machine whose heartbeat has timed out
     */
    /**
      todo: add by antony at: 2023/7/16
      心跳超时会调用
    */
    void notifyHeartbeatTimeout(ResourceID resourceID);

    /**
     * Callback which is called if a target specified by the given resource ID is no longer
     * reachable.
     *
     * @param resourceID resourceID identifying the target that is no longer reachable
     */
    /**
      todo: add by antony at: 2023/7/16

    */
    void notifyTargetUnreachable(ResourceID resourceID);

    /**
     * Callback which is called whenever a heartbeat with an associated payload is received. The
     * carried payload is given to this method.
     *
     * @param resourceID Resource ID identifying the sender of the payload
     * @param payload Payload of the received heartbeat
     */
    /**
      todo: add by antony at: 2023/7/16
      接收到有关心跳的payload会调用该方法
    */
    void reportPayload(ResourceID resourceID, I payload);

    /**
     * Retrieves the payload value for the next heartbeat message.
     *
     * @param resourceID Resource ID identifying the receiver of the payload
     * @return The payload for the next heartbeat
     */
    /**
      todo: add by antony at: 2023/7/16
      获取下一个心跳消息的payload
    */
    O retrievePayload(ResourceID resourceID);
}
