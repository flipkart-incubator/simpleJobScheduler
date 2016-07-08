package com.flipkart.kloud.httpjobscheduler.models;

import com.flipkart.kloud.httpjobscheduler.controllers.JobController;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class JobTest {
    @Test
    public void shouldBeDeletedShouldReturn_False_IfLessThanSelfDeleteThreshold() throws Exception {
        final ScheduledJob job = new ScheduledJob();
        ReflectionTestUtils.setField(job,"notFoundCount",new AtomicInteger(JobController.MAX_NOT_FOUND_ERRORS_BEFORE_SELF_DELETE -1));
        assertFalse(job.shouldBeSidelined());
    }

    @Test
    public void shouldBeDeletedShouldReturn_True_IfEqualToSelfDeleteThreshold() throws Exception {
        final ScheduledJob job = new ScheduledJob();
        ReflectionTestUtils.setField(job,"notFoundCount",new AtomicInteger(JobController.MAX_NOT_FOUND_ERRORS_BEFORE_SELF_DELETE));
        assertTrue(job.shouldBeSidelined());
    }

    @Test
    public void shouldBeDeletedShouldReturn_True_IfGreaterThanSelfDeleteThreshold() throws Exception {
        final ScheduledJob job = new ScheduledJob();
        ReflectionTestUtils.setField(job,"notFoundCount",new AtomicInteger(JobController.MAX_NOT_FOUND_ERRORS_BEFORE_SELF_DELETE+1));
        assertTrue(job.shouldBeSidelined());
    }

    @Test
    public void shouldSidelineWithReason() throws Exception {
        final ScheduledJob job = new ScheduledJob();
        assertFalse((Boolean) ReflectionTestUtils.getField(job,"sideLined"));
        ReflectionTestUtils.setField(job,"notFoundCount",new AtomicInteger(10));
        job.sidelineJob();
        assertTrue((Boolean) ReflectionTestUtils.getField(job, "sideLined"));
        assertThat(ReflectionTestUtils.getField(job,"sidelineReason"),is(ScheduledJob.MAX_NOT_FOUND_COUNT_EXCEEDED));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfJobSidelineIsAttemptedInIllegalState() throws Exception {
        final ScheduledJob job = new ScheduledJob();
        job.sidelineJob();
    }
}