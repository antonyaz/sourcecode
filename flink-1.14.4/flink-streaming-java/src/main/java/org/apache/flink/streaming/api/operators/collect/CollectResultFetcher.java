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

package org.apache.flink.streaming.api.operators.collect;

import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.api.common.accumulators.SerializedListAccumulator;
import org.apache.flink.api.common.typeutils.base.array.BytePrimitiveArraySerializer;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.core.execution.JobClient;
import org.apache.flink.runtime.dispatcher.UnavailableDispatcherOperationException;
import org.apache.flink.runtime.jobgraph.OperatorID;
import org.apache.flink.runtime.operators.coordination.CoordinationRequestGateway;
import org.apache.flink.util.ExceptionUtils;
import org.apache.flink.util.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** A fetcher which fetches query results from sink and provides exactly-once semantics. */
public class CollectResultFetcher<T> {

    private static final int DEFAULT_RETRY_MILLIS = 100;
    private static final long DEFAULT_ACCUMULATOR_GET_MILLIS = 10000;

    private static final Logger LOG = LoggerFactory.getLogger(CollectResultFetcher.class);

    private final AbstractCollectResultBuffer<T> buffer;

    private final CompletableFuture<OperatorID> operatorIdFuture;
    private final String accumulatorName;
    private final int retryMillis;

    @Nullable private JobClient jobClient;
    @Nullable private CoordinationRequestGateway gateway;

    private boolean jobTerminated;
    private boolean closed;

    public CollectResultFetcher(
            AbstractCollectResultBuffer<T> buffer,
            CompletableFuture<OperatorID> operatorIdFuture,
            String accumulatorName) {
        this(buffer, operatorIdFuture, accumulatorName, DEFAULT_RETRY_MILLIS);
    }

    CollectResultFetcher(
            AbstractCollectResultBuffer<T> buffer,
            CompletableFuture<OperatorID> operatorIdFuture,
            String accumulatorName,
            int retryMillis) {
        this.buffer = buffer;

        this.operatorIdFuture = operatorIdFuture;
        this.accumulatorName = accumulatorName;
        this.retryMillis = retryMillis;

        this.jobTerminated = false;
        this.closed = false;
    }

    public void setJobClient(JobClient jobClient) {
        Preconditions.checkArgument(
                jobClient instanceof CoordinationRequestGateway,
                "Job client must be a CoordinationRequestGateway. This is a bug.");
        this.jobClient = jobClient;
        this.gateway = (CoordinationRequestGateway) jobClient;
    }

    public T next() throws IOException {
        if (closed) {
            return null;
        }

        // this is to avoid sleeping before first try
        boolean beforeFirstTry = true;
        do {
            T res = buffer.next();
            if (res != null) {
                // we still have user-visible results, just use them
                return res;
            } else if (jobTerminated) {
                // no user-visible results, but job has terminated, we have to return
                return null;
            } else if (!beforeFirstTry) {
                // no results but job is still running, sleep before retry
                sleepBeforeRetry();
            }
            beforeFirstTry = false;

            if (isJobTerminated()) {
                // job terminated, read results from accumulator
                jobTerminated = true;
                Tuple2<Long, CollectCoordinationResponse> accResults = getAccumulatorResults();
                buffer.dealWithResponse(accResults.f1, accResults.f0);
                buffer.complete();
            } else {
                // job still running, try to fetch some results
                long requestOffset = buffer.getOffset();
                CollectCoordinationResponse response;
                try {
                    response = sendRequest(buffer.getVersion(), requestOffset);
                } catch (Exception e) {
                    if (ExceptionUtils.findThrowable(
                                    e, UnavailableDispatcherOperationException.class)
                            .isPresent()) {
                        LOG.debug("The job execution has not started yet; cannot fetch results.");
                    } else {
                        LOG.warn("An exception occurred when fetching query results", e);
                    }
                    continue;
                }
                // the response will contain data (if any) starting exactly from requested offset
                buffer.dealWithResponse(response, requestOffset);
            }
        } while (true);
    }

    public void close() {
        if (closed) {
            return;
        }

        cancelJob();
        closed = true;
    }

    private CollectCoordinationResponse sendRequest(String version, long offset)
            throws InterruptedException, ExecutionException {
        checkJobClientConfigured();

        OperatorID operatorId = operatorIdFuture.getNow(null);
        Preconditions.checkNotNull(operatorId, "Unknown operator ID. This is a bug.");

        CollectCoordinationRequest request = new CollectCoordinationRequest(version, offset);
        return (CollectCoordinationResponse)
                gateway.sendCoordinationRequest(operatorId, request).get();
    }

    private Tuple2<Long, CollectCoordinationResponse> getAccumulatorResults() throws IOException {
        checkJobClientConfigured();

        JobExecutionResult executionResult;
        try {
            // this timeout is sort of hack, see comments in isJobTerminated for explanation
            executionResult =
                    jobClient
                            .getJobExecutionResult()
                            .get(DEFAULT_ACCUMULATOR_GET_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new IOException("Failed to fetch job execution result", e);
        }

        ArrayList<byte[]> accResults = executionResult.getAccumulatorResult(accumulatorName);
        if (accResults == null) {
            // job terminates abnormally
            throw new IOException(
                    "Job terminated abnormally, no job execution result can be fetched");
        }

        try {
            List<byte[]> serializedResults =
                    SerializedListAccumulator.deserializeList(
                            accResults, BytePrimitiveArraySerializer.INSTANCE);
            byte[] serializedResult = serializedResults.get(0);
            return CollectSinkFunction.deserializeAccumulatorResult(serializedResult);
        } catch (ClassNotFoundException | IOException e) {
            // this is impossible
            throw new IOException("Failed to deserialize accumulator results", e);
        }
    }

    private boolean isJobTerminated() {
        checkJobClientConfigured();

        try {
            JobStatus status = jobClient.getJobStatus().get();
            return status.isGloballyTerminalState();
        } catch (Exception e) {
            // todo
            //  This is sort of hack.
            //  Currently different execution environment will have different behaviors
            //  when fetching a finished job status.
            //  For example, standalone session cluster will return a normal FINISHED,
            //  while mini cluster will throw IllegalStateException,
            //  and yarn per job will throw ApplicationNotFoundException.
            //  We have to assume that job has finished in this case.
            //  Change this when these behaviors are unified.
            LOG.warn(
                    "Failed to get job status so we assume that the job has terminated. Some data might be lost.",
                    e);
            return true;
        }
    }

    private void cancelJob() {
        checkJobClientConfigured();

        if (!isJobTerminated()) {
            jobClient.cancel();
        }
    }

    private void sleepBeforeRetry() {
        if (retryMillis <= 0) {
            return;
        }

        try {
            // todo a more proper retry strategy?
            Thread.sleep(retryMillis);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted when sleeping before a retry", e);
        }
    }

    private void checkJobClientConfigured() {
        Preconditions.checkNotNull(jobClient, "Job client must be configured before first use.");
        Preconditions.checkNotNull(
                gateway, "Coordination request gateway must be configured before first use.");
    }
}
