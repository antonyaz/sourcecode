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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.apache.druid.client.cache.CacheConfig;
import org.apache.druid.curator.ZkEnablementConfig;
import org.apache.druid.discovery.NodeRole;
import org.apache.druid.guice.CacheModule;
import org.apache.druid.guice.DruidProcessingModule;
import org.apache.druid.guice.HistoricalServiceModule;
import org.apache.druid.guice.Jerseys;
import org.apache.druid.guice.JoinableFactoryModule;
import org.apache.druid.guice.JsonConfigProvider;
import org.apache.druid.guice.LazySingleton;
import org.apache.druid.guice.LifecycleModule;
import org.apache.druid.guice.ManageLifecycle;
import org.apache.druid.guice.QueryRunnerFactoryModule;
import org.apache.druid.guice.QueryableModule;
import org.apache.druid.guice.ServerTypeConfig;
import org.apache.druid.java.util.common.logger.Logger;
import org.apache.druid.query.QuerySegmentWalker;
import org.apache.druid.query.lookup.LookupModule;
import org.apache.druid.server.QueryResource;
import org.apache.druid.server.ResponseContextConfig;
import org.apache.druid.server.SegmentManager;
import org.apache.druid.server.coordination.ServerManager;
import org.apache.druid.server.coordination.ServerType;
import org.apache.druid.server.coordination.ZkCoordinator;
import org.apache.druid.server.http.HistoricalResource;
import org.apache.druid.server.http.SegmentListerResource;
import org.apache.druid.server.http.SelfDiscoveryResource;
import org.apache.druid.server.initialization.jetty.JettyServerInitializer;
import org.apache.druid.server.metrics.QueryCountStatsProvider;
import org.apache.druid.timeline.PruneLastCompactionState;
import org.eclipse.jetty.server.Server;

import java.util.List;
import java.util.Properties;
import java.util.Set;

@Command(
    name = "historical",
    description = "Runs a Historical node, see https://druid.apache.org/docs/latest/Historical.html for a description"
)
public class CliHistorical extends ServerRunnable
{
  private static final Logger log = new Logger(CliHistorical.class);

  private boolean isZkEnabled = true;

  public CliHistorical()
  {
    super(log);
  }

  @Inject
  public void configure(Properties properties)
  {
    isZkEnabled = ZkEnablementConfig.isEnabled(properties);
  }

  /**
    todo: add by antony at: 2024/5/31
    节点角色仅为 Historical
  */
  @Override
  protected Set<NodeRole> getNodeRoles(Properties properties)
  {
    return ImmutableSet.of(NodeRole.HISTORICAL);
  }

  @Override
  protected List<? extends Module> getModules()
  {
    return ImmutableList.of(
            /**
              todo: add by antony at: 2024/5/31
             ，解析druid.processing 参数 及验证 直接内存
            */
        new DruidProcessingModule(),
        new QueryableModule(),
        /**
          todo: add by antony at: 2024/5/31    
          配置各种Query
        */
        new QueryRunnerFactoryModule(),
        new JoinableFactoryModule(),
        new HistoricalServiceModule(),
        binder -> {
          binder.bindConstant().annotatedWith(Names.named("serviceName")).to("druid/historical");
          binder.bindConstant().annotatedWith(Names.named("servicePort")).to(8083);
          binder.bindConstant().annotatedWith(Names.named("tlsServicePort")).to(8283);
          binder.bindConstant().annotatedWith(PruneLastCompactionState.class).to(true);
          binder.bind(ResponseContextConfig.class).toInstance(ResponseContextConfig.newConfig(true));

          /**
            todo: add by antony at: 2024/5/31    
            绑定 jetty server，启动web 服务
          */
          // register Server before binding ZkCoordinator to ensure HTTP endpoints are available immediately
          LifecycleModule.register(binder, Server.class);
          /**
            todo: add by antony at: 2024/5/31    
            用于绑定 Historical节点中的 查询 handler
          */
          binder.bind(ServerManager.class).in(LazySingleton.class);
          /**
            todo: add by antony at: 2024/5/31    
            维护数据源及状态信息，线程安全的
          */
          binder.bind(SegmentManager.class).in(LazySingleton.class);
          binder.bind(ZkCoordinator.class).in(ManageLifecycle.class);
          /**
            todo: add by antony at: 2024/5/31    
            绑定 querygSegmentWalker 
          */
          bindQuerySegmentWalker(binder);

          /**
            todo: add by antony at: 2024/5/31    
            绑定serverType节点为 historical
          */
          binder.bind(ServerTypeConfig.class).toInstance(new ServerTypeConfig(ServerType.HISTORICAL));
          /**
            todo: add by antony at: 2024/5/31    
            绑定jett 查询服务
          */
          binder.bind(JettyServerInitializer.class).to(QueryJettyServerInitializer.class).in(LazySingleton.class);
          binder.bind(QueryCountStatsProvider.class).to(QueryResource.class);
          Jerseys.addResource(binder, QueryResource.class);
          Jerseys.addResource(binder, SegmentListerResource.class);
          Jerseys.addResource(binder, HistoricalResource.class);
          LifecycleModule.register(binder, QueryResource.class);

          if (isZkEnabled) {
            LifecycleModule.register(binder, ZkCoordinator.class);
          }

          JsonConfigProvider.bind(binder, "druid.historical.cache", CacheConfig.class);
          binder.install(new CacheModule());

          bindAnnouncer(
              binder,
              DiscoverySideEffectsProvider.create()
          );

          Jerseys.addResource(binder, SelfDiscoveryResource.class);
          LifecycleModule.registerKey(binder, Key.get(SelfDiscoveryResource.class));
        },
        new LookupModule()
    );
  }

  /**
   * This method is visible for testing query retry on missing segments. See {@link CliHistoricalForQueryErrorTest}.
   */
  @VisibleForTesting
  public void bindQuerySegmentWalker(Binder binder)
  {
    binder.bind(QuerySegmentWalker.class).to(ServerManager.class).in(LazySingleton.class);
  }
}
