package com.flipkart.kloud.httpjobscheduler.util;

import com.flipkart.kloud.httpjobscheduler.models.Job;
import com.flipkart.kloud.httpjobscheduler.models.ScheduledJob;

/**
 * @understands: Tells the type of job
 */
public class JobHelper {

    /**
     * tells if job is Scheduled
     * @param job
     * @return
     */
    public static boolean isScheduled(Job job) {
        return job instanceof ScheduledJob;
    }
}
