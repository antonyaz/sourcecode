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

package org.apache.flink.runtime.scheduler.strategy;

import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.runtime.execution.ExecutionState;
import org.apache.flink.runtime.jobgraph.IntermediateResultPartitionID;
import org.apache.flink.runtime.scheduler.DeploymentOption;
import org.apache.flink.runtime.scheduler.ExecutionVertexDeploymentOption;
import org.apache.flink.runtime.scheduler.SchedulerOperations;
import org.apache.flink.util.IterableUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.flink.util.Preconditions.checkNotNull;
import static org.apache.flink.util.Preconditions.checkState;

/**
 * {@link SchedulingStrategy} instance which schedules tasks in granularity of pipelined regions.
 */
public class PipelinedRegionSchedulingStrategy implements SchedulingStrategy {

    private final SchedulerOperations schedulerOperations;

    private final SchedulingTopology schedulingTopology;

    private final DeploymentOption deploymentOption = new DeploymentOption(false);

    /** External consumer regions of each ConsumedPartitionGroup. */
    private final Map<ConsumedPartitionGroup, Set<SchedulingPipelinedRegion>>
            partitionGroupConsumerRegions = new IdentityHashMap<>();

    private final Map<SchedulingPipelinedRegion, List<ExecutionVertexID>> regionVerticesSorted =
            new IdentityHashMap<>();

    /** The ConsumedPartitionGroups which are produced by multiple regions. */
    private final Set<ConsumedPartitionGroup> crossRegionConsumedPartitionGroups =
            Collections.newSetFromMap(new IdentityHashMap<>());

    public PipelinedRegionSchedulingStrategy(
            final SchedulerOperations schedulerOperations,
            final SchedulingTopology schedulingTopology) {

        this.schedulerOperations = checkNotNull(schedulerOperations);
        this.schedulingTopology = checkNotNull(schedulingTopology);

        init();
    }

    private void init() {

        initCrossRegionConsumedPartitionGroups();

        initPartitionGroupConsumerRegions();

        for (SchedulingExecutionVertex vertex : schedulingTopology.getVertices()) {
            final SchedulingPipelinedRegion region =
                    schedulingTopology.getPipelinedRegionOfVertex(vertex.getId());
            regionVerticesSorted
                    .computeIfAbsent(region, r -> new ArrayList<>())
                    .add(vertex.getId());
        }
    }

    private void initCrossRegionConsumedPartitionGroups() {
        final Map<ConsumedPartitionGroup, Set<SchedulingPipelinedRegion>>
                producerRegionsByConsumedPartitionGroup = new IdentityHashMap<>();

        for (SchedulingPipelinedRegion pipelinedRegion :
                schedulingTopology.getAllPipelinedRegions()) {
            for (ConsumedPartitionGroup consumedPartitionGroup :
                    pipelinedRegion.getAllBlockingConsumedPartitionGroups()) {
                producerRegionsByConsumedPartitionGroup.computeIfAbsent(
                        consumedPartitionGroup, this::getProducerRegionsForConsumedPartitionGroup);
            }
        }

        for (SchedulingPipelinedRegion pipelinedRegion :
                schedulingTopology.getAllPipelinedRegions()) {
            for (ConsumedPartitionGroup consumedPartitionGroup :
                    pipelinedRegion.getAllBlockingConsumedPartitionGroups()) {
                final Set<SchedulingPipelinedRegion> producerRegions =
                        producerRegionsByConsumedPartitionGroup.get(consumedPartitionGroup);
                if (producerRegions.size() > 1 && producerRegions.contains(pipelinedRegion)) {
                    crossRegionConsumedPartitionGroups.add(consumedPartitionGroup);
                }
            }
        }
    }

    private Set<SchedulingPipelinedRegion> getProducerRegionsForConsumedPartitionGroup(
            ConsumedPartitionGroup consumedPartitionGroup) {
        final Set<SchedulingPipelinedRegion> producerRegions =
                Collections.newSetFromMap(new IdentityHashMap<>());
        for (IntermediateResultPartitionID partitionId : consumedPartitionGroup) {
            producerRegions.add(getProducerRegion(partitionId));
        }
        return producerRegions;
    }

    private SchedulingPipelinedRegion getProducerRegion(IntermediateResultPartitionID partitionId) {
        return schedulingTopology.getPipelinedRegionOfVertex(
                schedulingTopology.getResultPartition(partitionId).getProducer().getId());
    }

    private void initPartitionGroupConsumerRegions() {
        for (SchedulingPipelinedRegion region : schedulingTopology.getAllPipelinedRegions()) {
            for (ConsumedPartitionGroup consumedPartitionGroup :
                    region.getAllBlockingConsumedPartitionGroups()) {
                if (crossRegionConsumedPartitionGroups.contains(consumedPartitionGroup)
                        || isExternalConsumedPartitionGroup(consumedPartitionGroup, region)) {
                    partitionGroupConsumerRegions
                            .computeIfAbsent(consumedPartitionGroup, group -> new HashSet<>())
                            .add(region);
                }
            }
        }
    }

    @Override
    public void startScheduling() {
        /**
         *  todo: add by antony at 2022/5/4
         *  首先获取 血缘区域
         *  构造 PipelineRegion
         */
        final Set<SchedulingPipelinedRegion> sourceRegions =
                IterableUtils.toStream(schedulingTopology.getAllPipelinedRegions())
                        .filter(this::isSourceRegion)
                        .collect(Collectors.toSet());
        /**
         *  todo: add by antony at 2022/5/3
         *  按照 血缘区域  执行调度
         */
        maybeScheduleRegions(sourceRegions);
    }

    private boolean isSourceRegion(SchedulingPipelinedRegion region) {
        for (ConsumedPartitionGroup consumedPartitionGroup :
                region.getAllBlockingConsumedPartitionGroups()) {
            if (crossRegionConsumedPartitionGroups.contains(consumedPartitionGroup)
                    || isExternalConsumedPartitionGroup(consumedPartitionGroup, region)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void restartTasks(final Set<ExecutionVertexID> verticesToRestart) {
        final Set<SchedulingPipelinedRegion> regionsToRestart =
                verticesToRestart.stream()
                        .map(schedulingTopology::getPipelinedRegionOfVertex)
                        .collect(Collectors.toSet());
        maybeScheduleRegions(regionsToRestart);
    }

    @Override
    public void onExecutionStateChange(
            final ExecutionVertexID executionVertexId, final ExecutionState executionState) {
        if (executionState == ExecutionState.FINISHED) {
            final Set<ConsumedPartitionGroup> finishedConsumedPartitionGroups =
                    IterableUtils.toStream(
                                    schedulingTopology
                                            .getVertex(executionVertexId)
                                            .getProducedResults())
                            .filter(
                                    partition ->
                                            partition.getState() == ResultPartitionState.CONSUMABLE)
                            .flatMap(partition -> partition.getConsumedPartitionGroups().stream())
                            .filter(
                                    group ->
                                            crossRegionConsumedPartitionGroups.contains(group)
                                                    || group.areAllPartitionsFinished())
                            .collect(Collectors.toSet());

            final Set<SchedulingPipelinedRegion> consumerRegions =
                    finishedConsumedPartitionGroups.stream()
                            .flatMap(
                                    partitionGroup ->
                                            partitionGroupConsumerRegions
                                                    .getOrDefault(
                                                            partitionGroup, Collections.emptySet())
                                                    .stream())
                            .collect(Collectors.toSet());

            maybeScheduleRegions(consumerRegions);
        }
    }

    @Override
    public void onPartitionConsumable(final IntermediateResultPartitionID resultPartitionId) {}

    private void maybeScheduleRegions(final Set<SchedulingPipelinedRegion> regions) {
        final List<SchedulingPipelinedRegion> regionsSorted =
                SchedulingStrategyUtils.sortPipelinedRegionsInTopologicalOrder(
                        schedulingTopology, regions);

        final Map<ConsumedPartitionGroup, Boolean> consumableStatusCache = new HashMap<>();
        /**
         *  todo: add by antony at 2022/5/4
         *
         */
        for (SchedulingPipelinedRegion region : regionsSorted) {
            /**
             *  todo: add by antony at 2022/5/4
             *
             */
            maybeScheduleRegion(region, consumableStatusCache);
        }
    }

    private void maybeScheduleRegion(
            final SchedulingPipelinedRegion region,
            final Map<ConsumedPartitionGroup, Boolean> consumableStatusCache) {
        if (!areRegionInputsAllConsumable(region, consumableStatusCache)) {
            return;
        }

        checkState(
                areRegionVerticesAllInCreatedState(region),
                "BUG: trying to schedule a region which is not in CREATED state");

        /**
         *  todo: add by antony at 2022/5/4
         *  背景知识
         *  1、ExecutionGraph 中的每个任务顶点 叫做： ExecutionVertex
         *  2、ExecutionVertex 会指派 Task 的选项 ExecutionVertex => ExecutionVertexDeploymentOption
         *  3、ExecutionVertexDeploymentOption 会再次转换Wie DeploymentHandle
         *  4、DeploymentHandle 提供最终的部署逻辑的具体实现：提供了 deploy() 方法，底层实现就是调用 CurrentExecution.deploy()
         *  5、ExecutionVertex 这个任务顶点，每次执行的时候，都会初始化一个 CurrentExecution 的成员变量
         *      每次执行就是一个attempt： 一次尝试就是一次执行
         *  6、CurrentExecution.deploy() 执行一次，意味着 Task 部署一次
         */
        final List<ExecutionVertexDeploymentOption> vertexDeploymentOptions =
                SchedulingStrategyUtils.createExecutionVertexDeploymentOptions(
                        regionVerticesSorted.get(region), id -> deploymentOption);
        /**
         *  todo: add by antony at 2022/5/3
         *  这是 JobMaster 调度和部署的起点
         *  1、allocateSlots： 申请 这个 Job 所需要的所有的slot
         *  2、Deploy： 申请到了slot之后，执行 Task 的部署
         */
        schedulerOperations.allocateSlotsAndDeploy(vertexDeploymentOptions);
    }

    private boolean areRegionInputsAllConsumable(
            final SchedulingPipelinedRegion region,
            final Map<ConsumedPartitionGroup, Boolean> consumableStatusCache) {
        for (ConsumedPartitionGroup consumedPartitionGroup :
                region.getAllBlockingConsumedPartitionGroups()) {
            if (crossRegionConsumedPartitionGroups.contains(consumedPartitionGroup)) {
                if (!isCrossRegionConsumedPartitionConsumable(consumedPartitionGroup, region)) {
                    return false;
                }
            } else if (isExternalConsumedPartitionGroup(consumedPartitionGroup, region)) {
                if (!consumableStatusCache.computeIfAbsent(
                        consumedPartitionGroup, this::isConsumedPartitionGroupConsumable)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isConsumedPartitionGroupConsumable(
            final ConsumedPartitionGroup consumedPartitionGroup) {
        for (IntermediateResultPartitionID partitionId : consumedPartitionGroup) {
            if (schedulingTopology.getResultPartition(partitionId).getState()
                    != ResultPartitionState.CONSUMABLE) {
                return false;
            }
        }
        return true;
    }

    private boolean isCrossRegionConsumedPartitionConsumable(
            final ConsumedPartitionGroup consumedPartitionGroup,
            final SchedulingPipelinedRegion pipelinedRegion) {
        for (IntermediateResultPartitionID partitionId : consumedPartitionGroup) {
            if (isExternalConsumedPartition(partitionId, pipelinedRegion)
                    && schedulingTopology.getResultPartition(partitionId).getState()
                            != ResultPartitionState.CONSUMABLE) {
                return false;
            }
        }
        return true;
    }

    private boolean areRegionVerticesAllInCreatedState(final SchedulingPipelinedRegion region) {
        for (SchedulingExecutionVertex vertex : region.getVertices()) {
            if (vertex.getState() != ExecutionState.CREATED) {
                return false;
            }
        }
        return true;
    }

    private boolean isExternalConsumedPartitionGroup(
            ConsumedPartitionGroup consumedPartitionGroup,
            SchedulingPipelinedRegion pipelinedRegion) {

        return isExternalConsumedPartition(consumedPartitionGroup.getFirst(), pipelinedRegion);
    }

    private boolean isExternalConsumedPartition(
            IntermediateResultPartitionID partitionId, SchedulingPipelinedRegion pipelinedRegion) {
        return !pipelinedRegion.contains(
                schedulingTopology.getResultPartition(partitionId).getProducer().getId());
    }

    @VisibleForTesting
    Set<ConsumedPartitionGroup> getCrossRegionConsumedPartitionGroups() {
        return Collections.unmodifiableSet(crossRegionConsumedPartitionGroups);
    }

    /** The factory for creating {@link PipelinedRegionSchedulingStrategy}. */
    public static class Factory implements SchedulingStrategyFactory {
        @Override
        public SchedulingStrategy createInstance(
                final SchedulerOperations schedulerOperations,
                final SchedulingTopology schedulingTopology) {
            return new PipelinedRegionSchedulingStrategy(schedulerOperations, schedulingTopology);
        }
    }
}
