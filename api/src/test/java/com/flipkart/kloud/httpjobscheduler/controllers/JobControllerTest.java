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