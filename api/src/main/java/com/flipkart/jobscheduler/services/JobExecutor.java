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

import com.flipkart.jobscheduler.models.Api;
import com.flipkart.jobscheduler.models.Job;
import com.flipkart.jobscheduler.models.JobInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Executes a given job instance.
 * It is mandatory to have the implementation be async in nature to provide scalable execution.
 * @author yogesh.nachnani
 */
public abstract class JobExecutor<T extends Api> {
    private static final Logger log = LoggerFactory.getLogger(JobExecutor.class);

    void executeAsync(JobInstance jobInstance) {
        jobInstance.setScheduledTime(new Date().getTime());
        Job job = jobInstance.getJob();

        log.info("Executing {} delay {}", job.getName(), new Date().getTime() - jobInstance.getTimeToRun());

        if (job.markAsExecuting()) {
            _executeAsync((T) job.getApi(), new JobExecutionHandler(jobInstance));
        } else {
            log.info("Job {} is already running ", job.getName());
        }
    }

    protected abstract void _executeAsync(T api, JobExecutionHandler jobExecutionHandler);
}
