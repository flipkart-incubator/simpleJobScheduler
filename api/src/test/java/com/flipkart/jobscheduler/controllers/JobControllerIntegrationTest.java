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

package com.flipkart.jobscheduler.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.jobscheduler.Application;
import com.flipkart.jobscheduler.models.Job;
import com.flipkart.jobscheduler.repositories.Repository;
import com.flipkart.jobscheduler.services.MasterJobScheduler;
import com.flipkart.jobscheduler.utils.JobApiUtil;
import com.flipkart.jobscheduler.utils.TestApiCounter;
import com.flipkart.jobscheduler.utils.TestConstants;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@WebIntegrationTest
public class JobControllerIntegrationTest {
    @Autowired
    private Repository repository;

    @Autowired
    private MasterJobScheduler masterJobScheduler;

    @Autowired
    private TestApiCounter testApiCounter;

    private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        repository.deleteAll();
        masterJobScheduler.pause();
        masterJobScheduler.resume();
        testApiCounter.reset();
    }

    @Test
    public void testJobCreation_ShouldRunJobsAsPerSchedule() throws Exception {
        AsyncHttpClient.BoundRequestBuilder request = asyncHttpClient.preparePost("http://localhost:11000/jobs/scheduled").setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        request.setBody(objectMapper.writeValueAsString(JobApiUtil.createTestScheduledJob("testJob1", "http://localhost:11000/test", TestConstants.ONE_SECOND)));
        final ListenableFuture<Response> futureResponse = request.execute();
        final Response response = futureResponse.get();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        Thread.sleep(3 * TestConstants.ONE_SECOND + 100l);
        testApiCounter.assertHeard("test", 3);
        final Iterable<Job> allJobs = repository.findAll();
        assertThat(allJobs).extracting(Job::getName).containsExactly("testJob1");
    }

    @Test
    public void testOneTimeJobCreation_ShouldRunJobAsPerSchedule() throws JsonProcessingException, ExecutionException, InterruptedException {
        AsyncHttpClient.BoundRequestBuilder request = asyncHttpClient.preparePost("http://localhost:11000/jobs/oneTime").setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        request.setBody(objectMapper.writeValueAsString(JobApiUtil.createTestOneTimeJob("testJob1", "http://localhost:11000/test", System.currentTimeMillis() + 1000l)));
        final ListenableFuture<Response> futureResponse = request.execute();
        final Response response = futureResponse.get();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        Thread.sleep(TestConstants.ONE_SECOND + 200l);
        testApiCounter.assertHeard("test", 1);
        final Iterable<Job> allJobs = repository.findAll();
        assertThat(allJobs).isEmpty(); // one time job should be deleted
    }

    @Test
    public void testJobSidelining_ShouldSidelineJobsAfter404s() throws Exception {
        /* Creating legit job */
        AsyncHttpClient.BoundRequestBuilder request = asyncHttpClient.preparePost("http://localhost:11000/jobs/scheduled").setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        request.setBody(objectMapper.writeValueAsString(JobApiUtil.createTestScheduledJob("testJob1", "http://localhost:11000/test", TestConstants.ONE_SECOND)));
        final ListenableFuture<Response> futureResponse = request.execute();
        final Response response = futureResponse.get();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_OK);

        /* Creating Job that is destined to be sidelined*/
        AsyncHttpClient.BoundRequestBuilder request2 = asyncHttpClient.preparePost("http://localhost:11000/jobs/scheduled").setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        request2.setBody(objectMapper.writeValueAsString(JobApiUtil.createTestScheduledJob("testJob2", "http://localhost:11000/test/404", TestConstants.ONE_SECOND)));
        final ListenableFuture<Response> futureResponseForRequest2 = request2.execute();
        final Response response2 = futureResponseForRequest2.get();
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.SC_OK);

        Thread.sleep(5 * TestConstants.ONE_SECOND + 100l);
        testApiCounter.assertHeard("test", 5); // Legit job executed 5 times in 5 seconds
        testApiCounter.assertHeard("test404", 3); // Doomed job executed just thrice
        final Iterable<Job> allJobs = repository.findAll();
        assertThat(allJobs).extracting(Job::getName).containsExactly("testJob1","testJob2");
        assertThat(allJobs).extracting("sideLined",Boolean.class).containsExactly(false,true);
    }

    @Test
    public void testUnsidelining_ShouldUnsidelineGivenJob_AndRunAsPerSchedule() throws Exception {
        /* Creating Job that is destined to be sidelined*/
        AsyncHttpClient.BoundRequestBuilder request = asyncHttpClient.preparePost("http://localhost:11000/jobs/scheduled").setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        request.setBody(objectMapper.writeValueAsString(JobApiUtil.createTestScheduledJob("testJob2", "http://localhost:11000/test/404", TestConstants.ONE_SECOND)));
        final ListenableFuture<Response> futureResponseForRequest = request.execute();
        final Response response = futureResponseForRequest.get();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_OK);

        Thread.sleep(4 * TestConstants.ONE_SECOND + 100l);
        testApiCounter.assertHeard("test404", 3); // Doomed job executed just thrice
        final List<Job> allJobs = repository.findAll();
        assertThat(allJobs).extracting("sideLined", Boolean.class).containsExactly(true); // asserting job is sidelined

        AsyncHttpClient.BoundRequestBuilder requestUnSideline = asyncHttpClient.preparePut("http://localhost:11000/jobs/testJob2/unsideline").setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        final ListenableFuture<Response> unsidelineResponse = requestUnSideline.execute();
        assertThat(unsidelineResponse.get().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat((repository.findByName("testJob2").isSidelined())).isFalse(); // This check should happen within 3 seconds of the job being unsidelined. Mostly we are good

    }
}