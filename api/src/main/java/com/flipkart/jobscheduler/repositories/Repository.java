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

package com.flipkart.jobscheduler.repositories;

import com.flipkart.jobscheduler.models.Job;
import com.flipkart.jobscheduler.models.OneTimeJob;
import com.flipkart.jobscheduler.models.ScheduledJob;
import com.flipkart.jobscheduler.util.JobHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @understands: wrapper around all the Job repositories
 */

@Component
public class Repository {
    private OneTimeJobRepository oneTimeJobRepository;
    private ScheduledJobRepository scheduledJobRepository;

    @Autowired
    public Repository(OneTimeJobRepository oneTimeJobRepository, ScheduledJobRepository scheduledJobRepository) {
        this.oneTimeJobRepository = oneTimeJobRepository;
        this.scheduledJobRepository = scheduledJobRepository;
    }


    /**
     * gell all OneTimeJobs and ScheduledJobs
     * @return
     */
    public List<Job> findAll() {
        List<OneTimeJob> oneTimeJobs = oneTimeJobRepository.findAll();
        List<ScheduledJob> scheduledJobs = scheduledJobRepository.findAll();
        List<Job> jobs = new ArrayList<>();
        jobs.addAll(oneTimeJobs);
        jobs.addAll(scheduledJobs);
        return jobs;
    }

    /**
     * save the job
     * @param job
     * @return
     */
    public Job save(Job job) {
        if(JobHelper.isScheduled(job)) {
            return scheduledJobRepository.save((ScheduledJob) job);
        } else {
            return oneTimeJobRepository.save((OneTimeJob) job);
        }
    }

    /**
     * delete the job
     * @param job
     */
    public void delete(Job job) {
        if(JobHelper.isScheduled(job)) {
            scheduledJobRepository.delete((ScheduledJob) job);
        } else {
            oneTimeJobRepository.delete((OneTimeJob) job);
        }
    }

    /**
     * find the job by name
     * @param name
     * @return
     */
    public Job findByName(String name) {
         ScheduledJob job = scheduledJobRepository.findByName(name);
        if(job == null)
            return oneTimeJobRepository.findByName(name);
        return job;
    }

    /**
     * find the job by id
     * @param jobId
     * @return
     */
    public Job findOne(Long jobId) {
        ScheduledJob job = scheduledJobRepository.findOne(jobId);
        if(job == null)
            return oneTimeJobRepository.findOne(jobId);
        return job;
    }

    /**
     * delete all jobs
     */
    public void deleteAll() {
        oneTimeJobRepository.deleteAll();
        scheduledJobRepository.deleteAll();
    }
}
