package com.flipkart.kloud.httpjobscheduler.services;

import com.flipkart.kloud.httpjobscheduler.models.*;
import com.flipkart.kloud.httpjobscheduler.repositories.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.flipkart.kloud.httpjobscheduler.util.JobHelper.*;

import java.util.Date;
import java.util.concurrent.PriorityBlockingQueue;

public class JobScheduler {
    public static final Logger log = LoggerFactory.getLogger(JobScheduler.class);

    private PriorityBlockingQueue<JobInstance> jobInstances = new PriorityBlockingQueue<JobInstance>(10, new JobInstanceComparator());

    private Repository repository;
    private JobExecutor jobExecutor;

    public JobScheduler(JobExecutor jobExecutor, Repository repository) {
        this.jobExecutor = jobExecutor;
        this.repository = repository;
        thread = new SchedulerThread();
        thread.setUncaughtExceptionHandler(new ExceptionHandler());
        thread.start();
    }

    private SchedulerThread thread;

    public class SchedulerThread extends Thread {
        private static final long MAX_ALLOWED_DELAY = 5000;
        private Boolean halted = false;
        private Boolean paused = true;

        private final Object lock = new Object();

        @Override
        public void run() {
            while (true) {
                ensureNotPaused();

                synchronized (lock) {
                    if (halted) {
                        return;
                    }
                }

                JobInstance jobInstance = jobInstances.peek();

                if (jobInstance != null) {
                    if(jobInstance.shouldBeSidelined()) {
                        log.info("Self Deleting job {} since it seems it doesn;t exist on the other end of the world",jobInstance.getJob());
                        jobInstances.poll();
                        final Job job = jobInstance.getJob();
                        if(isScheduled(job)) {
                            ScheduledJob scheduledJob = (ScheduledJob) job;
                            scheduledJob.sidelineJob();
                        } else {
                            throw new IllegalStateException("Only scheduled jobs can be sidelined and job " + job.getName() + " is not one");
                        }
                        repository.save(job);
                    } else  if (jobInstance.shouldRunNow()) {
                        runSingleJob();
                    } else {
                        Long timeLeft = jobInstance.timeLeftToRun();
                        if (timeLeft > 0) {
                            log.info("Next job run only at {}",new Date(jobInstance.getTimeToRun()));
                            sleep(timeLeft);
                        }
                    }
                } else {
                    pauseJobExecution();
                }
            }
        }

        private void ensureNotPaused() {
            try {
                synchronized (lock) {
                    while (paused && !halted) {
                        log.info("Paused waiting for resume");
                        lock.wait();
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private void runSingleJob() {
            JobInstance jobInstance = jobInstances.poll();
            if (jobInstance != null) {
                if(!tooLateToRun(jobInstance)) {
                    jobExecutor.executeAsync(jobInstance);
                }
                Job job = jobInstance.getJob();

                // delete job if its ONE_TIME
                if(!isScheduled(job)) {
                    log.info("Deleting one time job {}", job.getName());
                    deleteJob(job);
                }
                else
                    addJob(job);
            }
        }

        private boolean tooLateToRun(JobInstance jobInstance) {
            return jobInstance.getTimeToRun() < System.currentTimeMillis() - MAX_ALLOWED_DELAY;
        }

        private void sleep(Long timeLeft) {
            try {
                synchronized (lock) {
                    lock.wait(timeLeft);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public void pauseJobExecution() {
            synchronized (lock) {
                log.info("Pausing job execution");
                paused = true;
                lock.notifyAll();
            }
        }

        public void resumeJobExecution() {
            synchronized (lock) {
                log.info("Resuming job execution");
                paused = false;
                lock.notifyAll();
            }
        }

        public void halt() {
            synchronized (lock) {
                log.info("Halting job execution");
                halted = true;
                lock.notifyAll();
            }
        }

        public void notifyNewJob() {
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    /**
     * deletes job completely
     * @param job
     */
    private void deleteJob(Job job) {
        removeJob(job.getName());
        repository.delete(job);
        log.info("job {} deleted", job.getName());
    }

    public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            e.printStackTrace();
            thread = new SchedulerThread();
            thread.setUncaughtExceptionHandler(this);
            thread.run();
        }
    }

    public synchronized void pause() {
        if (thread != null) {
            jobInstances.clear();
            
            thread.notifyNewJob();
        }
    }

    public void addJob(Job job) {
        JobInstance instance = job.nextInstance();

        if (instance != null) {
            log.info("Adding next instance of job {} at {} ",job.getName(),instance.getTimeToRun());
            jobInstances.add(instance);

            thread.resumeJobExecution();
        } else {
            deleteJob(job);
        }
    }

    /**
     * removes from the queue
     * @param jobName
     */
    public void removeJob(String jobName) {
        //Older implementation used remove by name. Keeping it the same for now. Name is a unique key in DB, so we're fine
        jobInstances.removeIf(jobInstance -> jobInstance.getJob().sameNameAs(jobName));
    }

    public PriorityBlockingQueue<JobInstance> getJobInstances() {
        return jobInstances;
    }
}