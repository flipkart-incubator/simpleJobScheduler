package com.flipkart.kloud.httpjobscheduler.models;

import java.util.Comparator;

/**
 * Created by gautam on 4/8/15.
 */
public class JobInstanceComparator implements Comparator<JobInstance> {
    @Override
    public int compare(JobInstance o1, JobInstance o2) {
        if(o1.getTimeToRun() < o2.getTimeToRun()) {
            return -1;
        } else {
            return 1;
        }
    }
}
