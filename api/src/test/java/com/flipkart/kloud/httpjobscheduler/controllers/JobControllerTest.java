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

package com.flipkart.kloud.httpjobscheduler.controllers;

import com.flipkart.kloud.httpjobscheduler.models.ScheduledJob;
import com.flipkart.kloud.httpjobscheduler.repositories.Repository;
import com.flipkart.kloud.httpjobscheduler.services.MasterJobScheduler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JobControllerTest {
    @Mock
    Repository repository;

    @Mock
    MasterJobScheduler jobScheduler;

    JobController jobController;

    @Before
    public void setUp() throws Exception {
        jobController = new JobController(repository,jobScheduler);
    }

    @Test
    public void shouldUnsidlineSidelinedJob() throws Exception {
        final ScheduledJob testJob = new ScheduledJob("testJob", null, null);
        ReflectionTestUtils.setField(testJob,"sideLined",true);
        when(repository.findByName("testJob")).thenReturn(testJob);
        jobController.unSideline("testJob");

        final ScheduledJob expectedUnSidelinedJob = new ScheduledJob("testJob",null,null);
        verify(jobScheduler,times(1)).addJob(expectedUnSidelinedJob);
        verifyNoMoreInteractions(jobScheduler);
        verify(repository,times(1)).save(expectedUnSidelinedJob);
    }

    @Test(expected = NotFoundException.class)
    public void testUnsideline_shouldReturnNotFoundForUnknownJobName() throws Exception {
        jobController.unSideline("testJob");
    }
}