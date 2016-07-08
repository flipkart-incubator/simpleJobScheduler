package com.flipkart.kloud.httpjobscheduler.services;

import com.flipkart.kloud.httpjobscheduler.Application;
import com.flipkart.kloud.httpjobscheduler.models.HttpApi;
import com.flipkart.kloud.httpjobscheduler.models.OneTimeJob;
import com.flipkart.kloud.httpjobscheduler.repositories.JobRepository;
import com.flipkart.kloud.httpjobscheduler.repositories.Repository;
import com.flipkart.kloud.httpjobscheduler.utils.TestConstants;
import com.ning.http.util.DateUtils;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@WebIntegrationTest
public class MasterJobSchedulerIntegrationTest extends TestCase {
    @Autowired
    private JobExecutor jobExecutor;

    @Autowired
    private Repository jobRepository;

    private MasterJobScheduler masterJobScheduler;

    private JobScheduler scheduler;

    @Before
    public void setUp() {
        jobRepository.deleteAll();
        scheduler = new JobScheduler(jobExecutor, jobRepository);
        masterJobScheduler = new MasterJobScheduler(jobRepository, new JobScheduler[]{scheduler});
    }

    @Test
    public void testAdd_shouldReAddToSameScheduler() {
        OneTimeJob oneTimeJob = new OneTimeJob("test", new HttpApi("test", HttpApi.Method.POST), TestConstants.fiveMinsFromNow());
        masterJobScheduler.addJob(oneTimeJob);
        assertThat(scheduler.getJobInstances().peek()).isNotNull();
        masterJobScheduler.addJob(oneTimeJob);
        assertThat(scheduler.getJobInstances().peek()).isNotNull();
    }
}