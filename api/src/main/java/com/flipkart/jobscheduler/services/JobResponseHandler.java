/*
 * Copyright 2012-2016, the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.flipkart.jobscheduler.services;

import com.codahale.metrics.SharedMetricRegistries;
import com.flipkart.jobscheduler.models.Job;
import com.flipkart.jobscheduler.models.JobInstance;
import com.flipkart.jobscheduler.models.ScheduledJob;
import com.flipkart.jobscheduler.util.JobHelper;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHandlerExtensions;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Date;

import static com.flipkart.jobscheduler.util.Constants.MAIN_METRIC_REGISTRY;

/**
 * A Handler created per <code>Api</code> invocation
 * The invocation being asynchronous in nature, the JobResponseHandler has methods that are invoked at various stages of
 * <code>Api</code> execution.
 */
public class JobResponseHandler extends AsyncCompletionHandler<Object> implements AsyncHandlerExtensions {
    private final JobInstance jobInstance;
    public static final Logger log = LoggerFactory.getLogger(JobExecutor.class);

    public JobResponseHandler(JobInstance jobInstance) {
        this.jobInstance = jobInstance;
    }

    @Override
    public Object onCompleted(Response response) throws Exception {

        onJobComplete(jobInstance, response);
        return null;
    }

    @Override
    public void onThrowable(Throwable t){
        SharedMetricRegistries.getOrCreate(MAIN_METRIC_REGISTRY).meter("jobs.failed").mark();
        Job job = jobInstance.getJob();
        JobExecutor.log.warn("Failed to execute " + job.getName(), t);

        job.markAsNotExecuting();
    }

    @Override
    public void onOpenConnection() {

    }

    @Override
    public void onConnectionOpen() {
        SharedMetricRegistries.getOrCreate(MAIN_METRIC_REGISTRY).counter("connections.open").inc();
    }

    @Override
    public void onPoolConnection() {

    }

    @Override
    public void onConnectionPooled() {

    }

    @Override
    public void onSendRequest(Object request) {
        onJobStarted(jobInstance);
    }

    @Override
    public void onRetry() {

    }

    @Override
    public void onDnsResolved(InetAddress address) {

    }

    @Override
    public void onSslHandshakeCompleted() {

    }

    private void onJobStarted(JobInstance jobInstance) {
        long currentTime = new Date().getTime();
        jobInstance.setJobStartedTime(currentTime);

        SharedMetricRegistries.getOrCreate(MAIN_METRIC_REGISTRY).meter("jobs.executing").mark();
        SharedMetricRegistries.getOrCreate(MAIN_METRIC_REGISTRY).histogram("jobs.nioDelay").update(currentTime - jobInstance.getScheduledTime());
        SharedMetricRegistries.getOrCreate(MAIN_METRIC_REGISTRY).histogram("jobs.totalDelay").update(currentTime - jobInstance.getTimeToRun());
    }

    private void onJobComplete(JobInstance jobInstance, Response response) {
        Job job = jobInstance.getJob();

        SharedMetricRegistries.getOrCreate(MAIN_METRIC_REGISTRY).meter("jobs.responseCode." + response.getStatusCode()).mark();
        long executionTime = new Date().getTime() - jobInstance.getJobStartedTime();
        SharedMetricRegistries.getOrCreate(MAIN_METRIC_REGISTRY).histogram("jobs.executionTime").update(executionTime);

        job.markAsNotExecuting();
        actOnStatusCode(job, response.getStatusCode());

        log.info("Finished executing {} in {} with response {} {} ", job.getName(), executionTime, response.getStatusCode(), response.getStatusText());
    }

    private void actOnStatusCode(Job job, int statusCode) {
        switch (statusCode) {
            /* TODO https://tools.ietf.org/html/rfc7231#section-6.5.4 states that 404 does not guarantee a permanent condition.
                Ideally, we should only sideline on 410
             */
            case 404 :
            case 410 :
                if(JobHelper.isScheduled(job)) {
                    ScheduledJob scheduledJob = (ScheduledJob) job;
                    scheduledJob.incrementNotFoundCount();
                }
                break;
        }
    }
}
