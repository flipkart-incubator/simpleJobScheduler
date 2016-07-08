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

import com.flipkart.jobscheduler.Application;
import com.flipkart.jobscheduler.models.HttpApi;
import com.flipkart.jobscheduler.models.OneTimeJob;
import com.flipkart.jobscheduler.repositories.Repository;
import com.flipkart.jobscheduler.utils.TestConstants;
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