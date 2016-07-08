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

package com.flipkart.kloud.httpjobscheduler.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.kloud.httpjobscheduler.controllers.JobController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
public class ScheduledJob extends Job {
    public static final Logger log = LoggerFactory.getLogger(ScheduledJob.class);
    public static final String MAX_NOT_FOUND_COUNT_EXCEEDED = "MAX_NOT_FOUND_COUNT_EXCEEDED";


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "schedule_id")
    @NotNull
    @JsonProperty
    private Schedule schedule;

    @Transient
    @JsonIgnore
    private AtomicInteger notFoundCount;

    @JsonProperty
    private boolean sideLined;

    @JsonProperty
    private String sidelineReason;

    ScheduledJob() {
        super();
        this.notFoundCount = new AtomicInteger(0);
        this.sideLined = false;
    }

    public ScheduledJob(String name, HttpApi httpApi, Schedule schedule) {
        super(name, httpApi);
        this.schedule = schedule;
        this.notFoundCount = new AtomicInteger(0);
        this.sideLined = false;
    }

    public JobInstance nextInstance() {
        Long timeToRun = schedule.nextRunTime();

        if(timeToRun != null) {
            return new JobInstance(this, timeToRun);
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScheduledJob job = (ScheduledJob) o;

        if (sideLined != job.sideLined) return false;
        return !(name != null ? !name.equals(job.name) : job.name != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (sideLined ? 1 : 0);
        return result;
    }

    public void incrementNotFoundCount() {
        notFoundCount.incrementAndGet();
    }

    public boolean shouldBeSidelined() {
        return hasExceededNotFoundCount();
    }

    private boolean hasExceededNotFoundCount() {
        return this.notFoundCount.get() >= JobController.MAX_NOT_FOUND_ERRORS_BEFORE_SELF_DELETE;
    }

    @Override
    public String toString() {
        return "Job{" +
            "name='" + name + '\'' +
            ", id=" + id +
            '}';
    }

    public void sidelineJob() {
        this.sidelineReason = figureProbableSidelineReason();
        this.sideLined = true;
    }

    private String figureProbableSidelineReason() {
        if (hasExceededNotFoundCount()) {
            return MAX_NOT_FOUND_COUNT_EXCEEDED;
        }
        throw new IllegalStateException("Trying to sideline a job for no valide reason");
    }

    public void unSideline() {
        this.sideLined = false;
        this.sidelineReason = null;
    }

    @JsonIgnore
    public boolean isSidelined() {
        return sideLined;
    }
}
