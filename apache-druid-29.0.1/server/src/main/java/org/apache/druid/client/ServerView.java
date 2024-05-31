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

package org.apache.druid.client;

import org.apache.druid.segment.realtime.appenderator.SegmentSchemas;
import org.apache.druid.server.coordination.DruidServerMetadata;
import org.apache.druid.timeline.DataSegment;

import java.util.concurrent.Executor;

/**
 */
/**
  todo: add by antony at: 2024/5/31
  不是很好翻译这个名称
 可以理解为接口回调通知
 1、集群中一个server被移除的回调
 2、集群中一个server中segment被操作后(加载、移除、schema被通知、segmentView初始化)的回调

 目前只有一个http的实现类
*/
public interface ServerView
{
  void registerServerRemovedCallback(Executor exec, ServerRemovedCallback callback);
  void registerSegmentCallback(Executor exec, SegmentCallback callback);

  enum CallbackAction
  {
    CONTINUE,
    UNREGISTER,
  }

  /**
    todo: add by antony at: 2024/5/31
    server移除后的回调
  */
  interface ServerRemovedCallback
  {
    /**
     * Called when a server is removed.
     *
     * The return value indicates if this callback has completed its work.  Note that even if this callback
     * indicates that it should be unregistered, it is not possible to guarantee that this callback will not
     * get called again.  There is a race condition between when this callback runs and other events that can cause
     * the callback to be queued for running.  Thus, callbacks shouldn't assume that they will not get called
     * again after they are done.  The contract is that the callback will eventually be unregistered, enforcing
     * a happens-before relationship is not part of the contract.
     *
     * @param server The server that was removed.
     * @return UNREGISTER if the callback has completed its work and should be unregistered.  CONTINUE if the callback
     * should remain registered.
     */
    CallbackAction serverRemoved(DruidServer server);
  }

  /**
    todo: add by antony at: 2024/5/31
    segment的系列回调
   1、在一个server中新增加segments回调
   2、在一个server中移除segments的回调
   3、segmentView初始化后的回调
   4、当segment schema 被announce的回调
   回调的返回结果就是 或者 继续 或者 未注册
  */
  interface SegmentCallback
  {
    /**
     * Called when a segment is added to a server.
     *
     * The return value indicates if this callback has completed its work.  Note that even if this callback
     * indicates that it should be unregistered, it is not possible to guarantee that this callback will not
     * get called again.  There is a race condition between when this callback runs and other events that can cause
     * the callback to be queued for running.  Thus, callbacks shouldn't assume that they will not get called
     * again after they are done.  The contract is that the callback will eventually be unregistered, enforcing
     * a happens-before relationship is not part of the contract.
     *
     * @param server The server that added a segment
     * @param segment The segment that was added
     * @return UNREGISTER if the callback has completed its work and should be unregistered.  CONTINUE if the callback
     * should remain registered.
     */
    CallbackAction segmentAdded(DruidServerMetadata server, DataSegment segment);

    /**
     * Called when a segment is removed from a server.
     *
     * The return value indicates if this callback has completed its work.  Note that even if this callback
     * indicates that it should be unregistered, it is not possible to guarantee that this callback will not
     * get called again.  There is a race condition between when this callback runs and other events that can cause
     * the callback to be queued for running.  Thus, callbacks shouldn't assume that they will not get called
     * again after they are done.  The contract is that the callback will eventually be unregistered, enforcing
     * a happens-before relationship is not part of the contract.
     *
     * @param server The server that removed a segment
     * @param segment The segment that was removed
     * @return UNREGISTER if the callback has completed its work and should be unregistered.  CONTINUE if the callback
     * should remain registered.
     */
    CallbackAction segmentRemoved(DruidServerMetadata server, DataSegment segment);

    CallbackAction segmentViewInitialized();

    /**
     * Called when segment schema is announced.
     *
     * @param segmentSchemas segment schema
     * @return continue or unregister
     */
    CallbackAction segmentSchemasAnnounced(SegmentSchemas segmentSchemas);
  }

  abstract class BaseSegmentCallback implements SegmentCallback
  {
    @Override
    public CallbackAction segmentAdded(DruidServerMetadata server, DataSegment segment)
    {
      return CallbackAction.CONTINUE;
    }

    @Override
    public CallbackAction segmentRemoved(DruidServerMetadata server, DataSegment segment)
    {
      return CallbackAction.CONTINUE;
    }

    @Override
    public CallbackAction segmentSchemasAnnounced(SegmentSchemas segmentSchemas)
    {
      return CallbackAction.CONTINUE;
    }

    @Override
    public CallbackAction segmentViewInitialized()
    {
      return CallbackAction.CONTINUE;
    }
  }
}
