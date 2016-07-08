package com.flipkart.kloud.httpjobscheduler.utils;

import com.flipkart.kloud.httpjobscheduler.models.JobInstance;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * @understands Matches job instances based only on the job that it contains
 */
public class JobInstanceMatcher extends TypeSafeMatcher<JobInstance> {

    private final JobInstance toCompare;

    public static JobInstanceMatcher matchJob(JobInstance jobInstance) {
        return new JobInstanceMatcher(jobInstance);
    }

    private JobInstanceMatcher(JobInstance toCompare) {
        this.toCompare = toCompare;
    }

    @Override
    protected boolean matchesSafely(JobInstance item) {
        return this.toCompare.getJob().equals(item.getJob());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("jobInstance(" + toCompare + ") has job " + toCompare.getJob());
    }
}
