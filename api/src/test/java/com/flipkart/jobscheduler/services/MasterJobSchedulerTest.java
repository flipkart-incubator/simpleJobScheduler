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

package com.flipkart.jobscheduler.services;

import com.flipkart.jobscheduler.controllers.JobController;
import com.flipkart.jobscheduler.models.ScheduledJob;
import com.flipkart.jobscheduler.models.Schedule;
import com.flipkart.jobscheduler.repositories.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MasterJobSchedulerTest {
    @Mock
    Repository repository;

    @Mock(name = "jobScheduler1")
    JobScheduler jobScheduler1;

    @Mock(name = "jobScheduler2")
    JobScheduler jobScheduler2;

    private MasterJobScheduler masterJobScheduler;

    private ScheduledJob job1;
    private ScheduledJob job2;

    @Before
    public void setUp() throws Exception {
        job1 = new ScheduledJob("job1", null, null);
        job2 = new ScheduledJob("job2", null, null);

        when(repository.findAll()).thenReturn(Arrays.asList(job1, job2));
        masterJobScheduler = new MasterJobScheduler(repository, new JobScheduler[]{ jobScheduler1, jobScheduler2});
    }

    @Test
    public void testResume_shouldDistributeJobsToJobSchedulers() throws Exception {
        masterJobScheduler.resume();
        verify(jobScheduler1,times(1)).addJob(job1);
        verifyNoMoreInteractions(jobScheduler1);
        verify(jobScheduler2,times(1)).addJob(job2);
        verifyNoMoreInteractions(jobScheduler2);
    }

    @Test
    public void testResume_shouldResumeOnlyNonSidelinedJobs() throws Exception {
        ReflectionTestUtils.setField(job1, "notFoundCount", new AtomicInteger(JobController.MAX_NOT_FOUND_ERRORS_BEFORE_SELF_DELETE));
        job1.sidelineJob();
        masterJobScheduler.resume();
        verify(jobScheduler1,times(1)).addJob(job2);
        verifyNoMoreInteractions(jobScheduler1);
        verifyNoMoreInteractions(jobScheduler2);
    }

    @Test
    public void testResume_shouldBeIdempotent() throws Exception {
        masterJobScheduler.resume();
        masterJobScheduler.resume();
        masterJobScheduler.resume();

        verify(jobScheduler1,times(1)).addJob(job1);
        verify(jobScheduler2,times(1)).addJob(job2);
        verifyNoMoreInteractions(jobScheduler1);
        verifyNoMoreInteractions(jobScheduler2);
    }
    @Test
    public void shouldRemoveJobFromRespectiveJobScheduler() throws Exception {
        masterJobScheduler.resume();
        masterJobScheduler.removeJob(job1);
        verify(jobScheduler1,times(1)).removeJob(job1.getName());
        masterJobScheduler.removeJob(job2);
        verify(jobScheduler2,times(1)).removeJob(job2.getName());
    }

    @Test
    public void testRemove_shouldHandleNonExistentJobGracefully() throws Exception {
        masterJobScheduler.removeJob(new ScheduledJob("nonExistentFooBar",null,null));
        assertTrue(true);
    }

    @Test
    public void testHalt_shouldHaltAllJobSchedulers() throws Exception {
        masterJobScheduler.pause();
        verify(jobScheduler1,times(1)).pause();
        verify(jobScheduler2,times(1)).pause();
        verifyNoMoreInteractions(jobScheduler1);
        verifyNoMoreInteractions(jobScheduler2);
    }

    @Test
    public void testAdd_shouldDistributeJobsWhileAdding() throws Exception {
        final ScheduledJob testJob1 = new ScheduledJob("testJob", null, null);
        masterJobScheduler.addJob(testJob1);
        verify(jobScheduler1,times(1)).addJob(testJob1);
        final ScheduledJob testJob2 = new ScheduledJob("testJob2", null, null);
        masterJobScheduler.addJob(testJob2);
        verify(jobScheduler2,times(1)).addJob(testJob2);
        verifyNoMoreInteractions(jobScheduler1);
        verifyNoMoreInteractions(jobScheduler2);
    }

    @Test
    public void testAdd_shouldHandleReAdditionOfJobs() throws Exception {
        final ScheduledJob testJob1 = new ScheduledJob("testJob", null, null);
        masterJobScheduler.addJob(testJob1);
        verify(jobScheduler1,times(1)).addJob(testJob1);
        final ScheduledJob testJobWithDifferentSchedule = new ScheduledJob("testJob", null, new Schedule(1l, 1l, 1l));
        masterJobScheduler.addJob(testJobWithDifferentSchedule);

        verify(jobScheduler1,times(1)).removeJob("testJob");
        verify(jobScheduler2,times(1)).addJob(testJobWithDifferentSchedule);
        verifyNoMoreInteractions(jobScheduler1);
        verifyNoMoreInteractions(jobScheduler2);
    }

    @Test
    public void testAdd_shouldAddAPreviouslyDeletedJob() throws Exception {
        final ScheduledJob testJob1 = new ScheduledJob("testJob", null, null);
        masterJobScheduler.addJob(testJob1);
        verify(jobScheduler1,times(1)).addJob(testJob1);
        masterJobScheduler.removeJob(testJob1);

        masterJobScheduler.addJob(testJob1);
        verify(jobScheduler2,times(1)).addJob(testJob1); // The second job scheduler is selected
    }

    @Test
    public void shouldResume_AfterBeingPaused() throws Exception {
        masterJobScheduler.pause(); // Scheduler paused
        masterJobScheduler.resume(); // Scheduler resumed again
        verify(jobScheduler1,times(1)).addJob(job1);
        verify(jobScheduler2,times(1)).addJob(job2);
    }
}