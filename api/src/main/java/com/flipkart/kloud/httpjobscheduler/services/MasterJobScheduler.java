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

package com.flipkart.kloud.httpjobscheduler.services;

import com.flipkart.kloud.httpjobscheduler.models.Job;
import com.flipkart.kloud.httpjobscheduler.repositories.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @understands Divides work among a list of job schedulers
 * TODO : Implement a better balancing algorithm. Current algorithm doesn't account for re-balancing in case of job removals
 */
@Component
public class MasterJobScheduler {

    private Repository repository;

    private JobScheduler[] jobSchedulers;
    private Integer index;
    private final Map<String, Integer> jobNamesToSchedulerMap;
    private AtomicBoolean started;

    @Autowired
    public MasterJobScheduler(Repository repository, @Value("${workers.count:1}") int numberOfJobSchedulers, JobExecutor jobExecutor) {
        /* Ideally, we would want to inject a list of Job schedulers, but spring does not allow an easy way to do that
        The @Resource annotation can be used only on fields/methods but not on constructors.
         */
        this(repository,createJobSchedulers(jobExecutor,numberOfJobSchedulers, repository));
    }

    MasterJobScheduler(Repository repository, JobScheduler[] jobSchedulers) {
        this.jobSchedulers = jobSchedulers;
        index = 0;
        this.repository = repository;
        jobNamesToSchedulerMap = new ConcurrentHashMap<>();
        started = new AtomicBoolean(false);
    }

    public void resume() {
        if (started.compareAndSet(false,true)) {
            // TODO : learn how to use JPA and write a proper query instead of this BS
            repository.findAll().stream().filter(job -> !job.isSidelined()).forEach(this::addJob);
        }
    }

    public void pause() {
        started.set(false);
        this.jobNamesToSchedulerMap.clear();
        for (JobScheduler jobScheduler : this.jobSchedulers) {
            jobScheduler.pause();
        }
    }
    public void removeJob(Job job) {
        final String jobName = job.getName();
        final Integer jobSchedulerIndex = this.jobNamesToSchedulerMap.get(jobName);
        if (jobSchedulerIndex!=null) {
            this.jobSchedulers[jobSchedulerIndex].removeJob(jobName);
            this.jobNamesToSchedulerMap.remove(jobName);
        }
    }

    public void addJob(Job job) {
        final Integer previousIndex = jobNamesToSchedulerMap.put(job.getName(), index);
        if (previousIndex != null) {
            this.jobSchedulers[previousIndex].removeJob(job.getName());
        }
        this.jobSchedulers[index].addJob(job);
        index = (index + 1) % jobSchedulers.length;
    }

    private static JobScheduler[] createJobSchedulers(JobExecutor jobExecutor, Integer numberOfJobSchedulers, Repository repository) {
        JobScheduler[] jobSchedulers = new JobScheduler[numberOfJobSchedulers];
        for (int i = 0; i < numberOfJobSchedulers ; i++) {
            jobSchedulers[i] = new JobScheduler(jobExecutor, repository);
        }
        return jobSchedulers;
    }
}
