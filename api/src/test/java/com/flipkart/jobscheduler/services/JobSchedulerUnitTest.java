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

import com.flipkart.jobscheduler.models.OneTimeJob;
import com.flipkart.jobscheduler.models.ScheduledJob;
import com.flipkart.jobscheduler.models.JobInstance;
import com.flipkart.jobscheduler.models.Schedule;
import com.flipkart.jobscheduler.repositories.Repository;
import com.flipkart.jobscheduler.utils.TestConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static com.flipkart.jobscheduler.utils.JobInstanceMatcher.matchJob;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JobSchedulerUnitTest {

    @Mock
    JobExecutor jobExecutor;

    @Mock
    Repository repository;

    @Test
    public void shouldNotRescheduleJobsToBeSidelined() throws Exception {
        final ScheduledJob jobThatWillBeSidelined = new ScheduledJob("job2", null, new Schedule(System.currentTimeMillis(), TestConstants.fiveMinsFromNow(), TestConstants.ONE_SECOND));
        final ScheduledJob jobThatWillNotBeSidelined = new ScheduledJob("job1", null, new Schedule(System.currentTimeMillis(), TestConstants.fiveMinsFromNow(), TestConstants.ONE_SECOND));

        doAnswer(invocation -> {
            final JobInstance jobInstance = (JobInstance) invocation.getArguments()[0];
            // Lets sideline this job
            ReflectionTestUtils.setField(jobInstance.getJob(),"notFoundCount",new AtomicInteger(10));
            return null;
        }).when(jobExecutor).executeAsync(argThat(matchJob(new JobInstance(jobThatWillBeSidelined, 1l))));
        final JobScheduler jobScheduler = new JobScheduler(jobExecutor, repository);
        jobScheduler.addJob(jobThatWillNotBeSidelined);
        jobScheduler.addJob(jobThatWillBeSidelined);

        Thread.sleep(3100l);
        jobScheduler.pause();

        verify(jobExecutor,times(1)).executeAsync(argThat(matchJob(new JobInstance(jobThatWillBeSidelined, 1l))));
        verify(jobExecutor,times(3)).executeAsync(argThat(matchJob(new JobInstance(jobThatWillNotBeSidelined, 1l))));
        verifyNoMoreInteractions(jobExecutor);
    }

    @Test
    public void testAddJobs() throws Exception {
        final JobScheduler jobScheduler = new JobScheduler(jobExecutor, repository);
        final ScheduledJob testJob = new ScheduledJob("job1", null, new Schedule(System.currentTimeMillis(), TestConstants.fiveMinsFromNow(), TestConstants.FIVE_MINS_MILLIS));
        jobScheduler.addJob(testJob);
        assertThat(((PriorityBlockingQueue) ReflectionTestUtils.getField(jobScheduler, "jobInstances")).size(), is(1));
        assertThat(((PriorityBlockingQueue) ReflectionTestUtils.getField(jobScheduler, "jobInstances")).poll(), is(testJob.nextInstance()));
    }

    @Test
    public void shouldUpdateSidelinedJobsInDb() throws Exception {
        final ScheduledJob jobThatWillBeSidelined = new ScheduledJob("job2", null, new Schedule(System.currentTimeMillis(), TestConstants.fiveMinsFromNow(), TestConstants.ONE_SECOND));
        final ScheduledJob jobThatWillNotBeSidelined = new ScheduledJob("job1", null, new Schedule(System.currentTimeMillis(), TestConstants.fiveMinsFromNow(), TestConstants.ONE_SECOND));

        doAnswer(invocation -> {
            final JobInstance jobInstance = (JobInstance) invocation.getArguments()[0];
            // Lets sideline this job
            ReflectionTestUtils.setField(jobInstance.getJob(),"notFoundCount",new AtomicInteger(10));
            return null;
        }).when(jobExecutor).executeAsync(argThat(matchJob(new JobInstance(jobThatWillBeSidelined, 1l))));
        final JobScheduler jobScheduler = new JobScheduler(jobExecutor, repository);
        jobScheduler.addJob(jobThatWillNotBeSidelined);
        jobScheduler.addJob(jobThatWillBeSidelined);

        Thread.sleep(2100l);
        jobScheduler.pause();

        verify(repository,times(1)).save(jobThatWillBeSidelined);
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void shouldDeleteOneTimeJobAfterRun() throws InterruptedException {
        final JobScheduler jobScheduler = new JobScheduler(jobExecutor, repository);
        final OneTimeJob oneTimeJob = new OneTimeJob("job1", null, TestConstants.oneSecondFromNow());
        jobScheduler.addJob(oneTimeJob);
        Thread.sleep(1100l);
        jobScheduler.pause();
        verify(repository, times(1)).delete(oneTimeJob);
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void shouldDeleteExpiredJob() {
        final JobScheduler jobScheduler = new JobScheduler(jobExecutor, repository);
        final ScheduledJob expiredJob = new ScheduledJob("job2", null, new Schedule(System.currentTimeMillis(), TestConstants.oneMinBack(), TestConstants.ONE_SECOND));
        final ScheduledJob activeJob = new ScheduledJob("job1", null, new Schedule(System.currentTimeMillis(), TestConstants.fiveMinsFromNow(), TestConstants.ONE_SECOND));
        jobScheduler.addJob(expiredJob);
        jobScheduler.addJob(activeJob);
        verify(repository, times(1)).delete(expiredJob);
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void shouldDeleteExpiredOneTimeJob() {
        final JobScheduler jobScheduler = new JobScheduler(jobExecutor, repository);
        final OneTimeJob expiredOneTimeJob = new OneTimeJob("job1", null, TestConstants.oneMinBack());
        final OneTimeJob activeOneTimeJob = new OneTimeJob("job2", null, TestConstants.fiveMinsFromNow());
        jobScheduler.addJob(expiredOneTimeJob);
        jobScheduler.addJob(activeOneTimeJob);
        verify(repository, times(1)).delete(expiredOneTimeJob);
        verifyZeroInteractions(repository);
    }
}