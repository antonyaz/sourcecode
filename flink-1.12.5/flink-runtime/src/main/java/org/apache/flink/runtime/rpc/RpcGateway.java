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

package org.apache.flink.runtime.rpc;

/**
 * TODO: 跟Hadoop中的Protocol 通信协议一样
 * 跟Spark中的RpcEndpoint一致
 *  子类特别多
 *  作为Rpc服务组件
 *  FencedRpcGateway -> JobMasterGateway/DispatcherGateway/ResourceManagerGateway
 */
/** Rpc gateway interface which has to be implemented by Rpc gateways. */
public interface RpcGateway {

    /**
     * Returns the fully qualified address under which the associated rpc endpoint is reachable.
     *
     * @return Fully qualified (RPC) address under which the associated rpc endpoint is reachable
     */
    String getAddress();

    /**
     * Returns the fully qualified hostname under which the associated rpc endpoint is reachable.
     *
     * @return Fully qualified hostname under which the associated rpc endpoint is reachable
     */
    String getHostname();
}
