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

package com.flipkart.jobscheduler.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.jobscheduler.controllers.JobController;
import com.flipkart.jobscheduler.utils.JobApiUtil;
import com.flipkart.jobscheduler.utils.TestConstants;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class JobTest {
    private ObjectMapper objectMapper = new ObjectMapper();
    @Test
    public void testFoo() throws Exception {
        String json = objectMapper.writeValueAsString(JobApiUtil.createTestScheduledJob("testJob1", "http://localhost:11000/test", TestConstants.ONE_SECOND));
        final ScheduledJob scheduledJob = objectMapper.readValue(json, ScheduledJob.class);
        System.out.println(scheduledJob);
    }

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