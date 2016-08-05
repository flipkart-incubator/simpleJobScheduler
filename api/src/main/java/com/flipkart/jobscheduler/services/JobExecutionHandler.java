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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static com.flipkart.jobscheduler.util.Constants.MAIN_METRIC_REGISTRY;

public class JobExecutionHandler {
    /*
        Used to model the result of the API invocation.
        Each RPC mechanism will have its own "status codes" which should be mapped to these broad buckets
        Technically, we don't care if a "SUCCESS" came from an HTTP equivalent of 200 or 202.
        The Job Execution mechansim cares if it needs to retry or not.
        Presently, we retry indefinitely on TRANSIENT_FAILURES and for a fixed number of "x" times on PERMANENT_FAILURE
     */
    public enum JobStatus {
        SUCCESS,
        TRANSIENT_FAILURE,
        PERMANENT_FAILURE;

    }
    private static final Logger log = LoggerFactory.getLogger(JobExecutionHandler.class);
    private JobInstance jobInstance;

    public JobExecutionHandler(JobInstance jobInstance) {
        this.jobInstance = jobInstance;
    }

    public void onJobStarted(){
        long currentTime = new Date().getTime();
        jobInstance.setJobStartedTime(currentTime);

        SharedMetricRegistries.getOrCreate(MAIN_METRIC_REGISTRY).meter("jobs.executing").mark();
        SharedMetricRegistries.getOrCreate(MAIN_METRIC_REGISTRY).histogram("jobs.nioDelay").update(currentTime - jobInstance.getScheduledTime());
        SharedMetricRegistries.getOrCreate(MAIN_METRIC_REGISTRY).histogram("jobs.totalDelay").update(currentTime - jobInstance.getTimeToRun());
    }

    public void onException(Throwable t) {
        SharedMetricRegistries.getOrCreate(MAIN_METRIC_REGISTRY).meter("jobs.failed").mark();
        Job job = jobInstance.getJob();
        log.warn("Failed to execute " + job.getName(), t);

        job.markAsNotExecuting();
    }

    public void onJobCompleted(JobStatus jobStatus) {

        SharedMetricRegistries.getOrCreate(MAIN_METRIC_REGISTRY).meter("jobs.responseCode." + jobStatus.name()).mark();
        long executionTime = new Date().getTime() - jobInstance.getJobStartedTime();
        SharedMetricRegistries.getOrCreate(MAIN_METRIC_REGISTRY).histogram("jobs.executionTime").update(executionTime);

        Job job = jobInstance.getJob();
        job.markAsNotExecuting();
        switch (jobStatus) {
            case PERMANENT_FAILURE:
                if(JobHelper.isScheduled(job)) {
                    ScheduledJob scheduledJob = (ScheduledJob) job;
                    scheduledJob.incrementNotFoundCount();
                }
        }
        log.info("Finished executing {} in {} with status {} ", job.getName(), executionTime, jobStatus);
    }
}
