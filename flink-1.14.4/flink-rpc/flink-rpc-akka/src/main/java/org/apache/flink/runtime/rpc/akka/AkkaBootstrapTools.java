/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.runtime.rpc.akka;

import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.configuration.AkkaOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.rpc.RpcSystem;
import org.apache.flink.util.NetUtils;

import org.apache.flink.shaded.netty4.io.netty.channel.ChannelException;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.BindException;
import java.util.Iterator;
import java.util.Optional;

/** Tools for starting the Actor Systems used to run the JobManager and TaskManager actors. */
public class AkkaBootstrapTools {
    /**
     * Starts a remote ActorSystem at given address and specific port range.
     *
     * @param configuration The Flink configuration
     * @param externalAddress The external address to access the ActorSystem.
     * @param externalPortRange The choosing range of the external port to access the ActorSystem.
     * @param logger The logger to output log information.
     * @return The ActorSystem which has been started
     * @throws Exception Thrown when actor system cannot be started in specified port range
     */
    @VisibleForTesting
    public static ActorSystem startRemoteActorSystem(
            Configuration configuration,
            String externalAddress,
            String externalPortRange,
            Logger logger)
            throws Exception {
        return startRemoteActorSystem(
                configuration,
                AkkaUtils.getFlinkActorSystemName(),
                externalAddress,
                externalPortRange,
                NetUtils.getWildcardIPAddress(),
                Optional.empty(),
                logger,
                AkkaUtils.getForkJoinExecutorConfig(
                        getForkJoinExecutorConfiguration(configuration)),
                null);
    }

    /**
     * Starts a remote ActorSystem at given address and specific port range.
     *
     * @param configuration The Flink configuration
     * @param actorSystemName Name of the started {@link ActorSystem}
     * @param externalAddress The external address to access the ActorSystem.
     * @param externalPortRange The choosing range of the external port to access the ActorSystem.
     * @param bindAddress The local address to bind to.
     * @param bindPort The local port to bind to. If not present, then the external port will be
     *     used.
     * @param logger The logger to output log information.
     * @param actorSystemExecutorConfiguration configuration for the ActorSystem's underlying
     *     executor
     * @param customConfig Custom Akka config to be combined with the config derived from Flink
     *     configuration.
     * @return The ActorSystem which has been started
     * @throws Exception Thrown when actor system cannot be started in specified port range
     */
    public static ActorSystem startRemoteActorSystem(
            Configuration configuration,
            String actorSystemName,
            String externalAddress,
            String externalPortRange,
            String bindAddress,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Integer> bindPort,
            Logger logger,
            Config actorSystemExecutorConfiguration,
            Config customConfig)
            throws Exception {

        // parse port range definition and create port iterator
        Iterator<Integer> portsIterator;
        try {
            portsIterator = NetUtils.getPortRangeFromString(externalPortRange);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid port range definition: " + externalPortRange);
        }

        while (portsIterator.hasNext()) {
            final int externalPort = portsIterator.next();

            try {
                /**
                  todo: add by antony at: 2023/7/16
                  创建
                */
                return startRemoteActorSystem(
                        configuration,
                        actorSystemName,
                        externalAddress,
                        externalPort,
                        bindAddress,
                        bindPort.orElse(externalPort),
                        logger,
                        actorSystemExecutorConfiguration,
                        customConfig);
            } catch (Exception e) {
                // we can continue to try if this contains a netty channel exception
                Throwable cause = e.getCause();
                if (!(cause instanceof org.jboss.netty.channel.ChannelException
                        || cause instanceof java.net.BindException)) {
                    throw e;
                } // else fall through the loop and try the next port
            }
        }

        // if we come here, we have exhausted the port range
        throw new BindException(
                "Could not start actor system on any port in port range " + externalPortRange);
    }

    /**
     * Starts a remote Actor System at given address and specific port.
     *
     * @param configuration The Flink configuration.
     * @param actorSystemName Name of the started {@link ActorSystem}
     * @param externalAddress The external address to access the ActorSystem.
     * @param externalPort The external port to access the ActorSystem.
     * @param bindAddress The local address to bind to.
     * @param bindPort The local port to bind to.
     * @param logger the logger to output log information.
     * @param actorSystemExecutorConfiguration configuration for the ActorSystem's underlying
     *     executor
     * @param customConfig Custom Akka config to be combined with the config derived from Flink
     *     configuration.
     * @return The ActorSystem which has been started.
     * @throws Exception
     */
    private static ActorSystem startRemoteActorSystem(
            Configuration configuration,
            String actorSystemName,
            String externalAddress,
            int externalPort,
            String bindAddress,
            int bindPort,
            Logger logger,
            Config actorSystemExecutorConfiguration,
            Config customConfig)
            throws Exception {

        String externalHostPortUrl =
                NetUtils.unresolvedHostAndPortToNormalizedString(externalAddress, externalPort);
        String bindHostPortUrl =
                NetUtils.unresolvedHostAndPortToNormalizedString(bindAddress, bindPort);
        logger.info(
                "Trying to start actor system, external address {}, bind address {}.",
                externalHostPortUrl,
                bindHostPortUrl);

        try {
            Config akkaConfig =
                    AkkaUtils.getAkkaConfig(
                            configuration,
                            new HostAndPort(externalAddress, externalPort),
                            new HostAndPort(bindAddress, bindPort),
                            actorSystemExecutorConfiguration);

            if (customConfig != null) {
                akkaConfig = customConfig.withFallback(akkaConfig);
            }

            /**
              todo: add by antony at: 2023/7/16
              启动
            */
            return startActorSystem(akkaConfig, actorSystemName, logger);
        } catch (Throwable t) {
            if (t instanceof ChannelException) {
                Throwable cause = t.getCause();
                if (cause != null && t.getCause() instanceof BindException) {
                    throw new IOException(
                            "Unable to create ActorSystem at address "
                                    + bindHostPortUrl
                                    + " : "
                                    + cause.getMessage(),
                            t);
                }
            }
            throw new Exception("Could not create actor system", t);
        }
    }

    /**
     * Starts a local Actor System.
     *
     * @param configuration The Flink configuration.
     * @param actorSystemName Name of the started ActorSystem.
     * @param logger The logger to output log information.
     * @param actorSystemExecutorConfiguration Configuration for the ActorSystem's underlying
     *     executor.
     * @param customConfig Custom Akka config to be combined with the config derived from Flink
     *     configuration.
     * @return The ActorSystem which has been started.
     * @throws Exception
     */
    public static ActorSystem startLocalActorSystem(
            Configuration configuration,
            String actorSystemName,
            Logger logger,
            Config actorSystemExecutorConfiguration,
            Config customConfig)
            throws Exception {

        logger.info("Trying to start local actor system");

        try {
            Config akkaConfig =
                    AkkaUtils.getAkkaConfig(
                            configuration, null, null, actorSystemExecutorConfiguration);

            if (customConfig != null) {
                akkaConfig = customConfig.withFallback(akkaConfig);
            }

            return startActorSystem(akkaConfig, actorSystemName, logger);
        } catch (Throwable t) {
            throw new Exception("Could not create actor system", t);
        }
    }

    /**
     * Starts an Actor System with given Akka config.
     *
     * @param akkaConfig Config of the started ActorSystem.
     * @param actorSystemName Name of the started ActorSystem.
     * @param logger The logger to output log information.
     * @return The ActorSystem which has been started.
     */
    private static ActorSystem startActorSystem(
            Config akkaConfig, String actorSystemName, Logger logger) {
        logger.debug("Using akka configuration\n {}", akkaConfig);
        ActorSystem actorSystem = AkkaUtils.createActorSystem(actorSystemName, akkaConfig);

        logger.info("Actor system started at {}", AkkaUtils.getAddress(actorSystem));
        return actorSystem;
    }

    // ------------------------------------------------------------------------

    /** Private constructor to prevent instantiation. */
    private AkkaBootstrapTools() {}

    /** Configuration interface for {@link ActorSystem} underlying executor. */
    public interface ActorSystemExecutorConfiguration {

        /**
         * Create the executor {@link Config} for the respective executor.
         *
         * @return Akka config for the respective executor
         */
        Config getAkkaConfig();
    }

    public static RpcSystem.ForkJoinExecutorConfiguration getForkJoinExecutorConfiguration(
            final Configuration configuration) {
        final double parallelismFactor =
                configuration.getDouble(AkkaOptions.FORK_JOIN_EXECUTOR_PARALLELISM_FACTOR);
        final int minParallelism =
                configuration.getInteger(AkkaOptions.FORK_JOIN_EXECUTOR_PARALLELISM_MIN);
        final int maxParallelism =
                configuration.getInteger(AkkaOptions.FORK_JOIN_EXECUTOR_PARALLELISM_MAX);

        return new RpcSystem.ForkJoinExecutorConfiguration(
                parallelismFactor, minParallelism, maxParallelism);
    }
}
