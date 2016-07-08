package com.flipkart.kloud.httpjobscheduler.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by gautam on 4/8/15.
 */
public class JobInstance {
    public static final Logger log = LoggerFactory.getLogger(JobInstance.class);

    private Job job;
    private Long timeToRun;
    private Long scheduledTime;
    private Long jobStartedTime;

    public JobInstance(Job job, Long timeToRun) {
        this.job = job;
        this.timeToRun = timeToRun;
    }

    public Long getTimeToRun() {
        return timeToRun;
    }

    public Job getJob() {
        return job;
    }

    public boolean shouldRunNow() {
        return timeToRun <= new Date().getTime();
    }

    public Long timeLeftToRun() {
        return timeToRun - new Date().getTime();
    }

    public Long getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Long scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public void setJobStartedTime(long jobStartedTime) {
        this.jobStartedTime = jobStartedTime;
    }

    public Long getJobStartedTime() {
        return jobStartedTime;
    }

    public boolean shouldBeSidelined() {
        return this.getJob().shouldBeSidelined();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobInstance that = (JobInstance) o;

        if (job != null ? !job.equals(that.job) : that.job != null) return false;
        if (timeToRun != null ? !timeToRun.equals(that.timeToRun) : that.timeToRun != null) return false;
        if (scheduledTime != null ? !scheduledTime.equals(that.scheduledTime) : that.scheduledTime != null)
            return false;
        return !(jobStartedTime != null ? !jobStartedTime.equals(that.jobStartedTime) : that.jobStartedTime != null);

    }

    @Override
    public int hashCode() {
        int result = job != null ? job.hashCode() : 0;
        result = 31 * result + (timeToRun != null ? timeToRun.hashCode() : 0);
        result = 31 * result + (scheduledTime != null ? scheduledTime.hashCode() : 0);
        result = 31 * result + (jobStartedTime != null ? jobStartedTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JobInstance{" +
            "job=" + job +
            ", timeToRun=" + timeToRun +
            ", scheduledTime=" + scheduledTime +
            ", jobStartedTime=" + jobStartedTime +
            '}';
    }
}
