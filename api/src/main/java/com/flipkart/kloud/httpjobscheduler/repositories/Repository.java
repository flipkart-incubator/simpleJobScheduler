package com.flipkart.kloud.httpjobscheduler.repositories;

import com.flipkart.kloud.httpjobscheduler.models.Job;
import com.flipkart.kloud.httpjobscheduler.models.OneTimeJob;
import com.flipkart.kloud.httpjobscheduler.models.ScheduledJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static com.flipkart.kloud.httpjobscheduler.util.JobHelper.*;

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
        if(isScheduled(job)) {
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
        if(isScheduled(job)) {
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
