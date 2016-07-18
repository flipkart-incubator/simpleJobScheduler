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

import com.codahale.metrics.*;
import com.flipkart.jobscheduler.models.Job;
import com.flipkart.jobscheduler.models.HttpApi;
import com.flipkart.jobscheduler.models.ScheduledJob;
import com.flipkart.jobscheduler.models.JobInstance;
import com.flipkart.jobscheduler.util.JobHelper;
import com.ning.http.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JobExecutor  implements InitializingBean {
    public static final Logger log = LoggerFactory.getLogger(JobExecutor.class);

    @Autowired
    private MetricRegistry metricRegistry;

    private Histogram nioDelay;
    private Histogram totalDelay;
    private Histogram jobExecutionTime;
    private Meter jobsExecuting;
    private Counter connectionsOpen;
    private Meter failedJobs;
    private Map<Integer, Meter> responseCodes = new HashMap<>();

    private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
    private JmxReporter reporter;

    @Override
    public void afterPropertiesSet() throws Exception {
        nioDelay = metricRegistry.histogram("jobs.nioDelay");
        totalDelay = metricRegistry.histogram("jobs.totalDelay");
        jobExecutionTime = metricRegistry.histogram("jobs.executionTime");
        jobsExecuting = metricRegistry.meter("jobs.executing");
        connectionsOpen = metricRegistry.counter("connections.open");
        failedJobs = metricRegistry.meter("jobs.failed");

        reporter = JmxReporter.forRegistry(metricRegistry).build();
        reporter.start();
    }

    public void executeAsync(JobInstance jobInstance) {
        jobInstance.setScheduledTime(new Date().getTime());
        Job job = jobInstance.getJob();

        log.info("Executing {} delay {}",job.getName(),new Date().getTime() - jobInstance.getTimeToRun());

        if(job.markAsExecuting()) {
            callHttpApi(jobInstance, asyncHttpClient);
        } else {
            log.info("Job {} is already running ");
        }
    }

    private void callHttpApi(JobInstance jobInstance, AsyncHttpClient asyncHttpClient) {
        Job job = jobInstance.getJob();
        HttpApi httpApi = job.getHttpApi();
        HttpApi.Method method = httpApi.getMethod();

        AsyncHttpClient.BoundRequestBuilder request;

        if (method == HttpApi.Method.GET) {
            request = asyncHttpClient.prepareGet(httpApi.getUrl());
        } else if (method == HttpApi.Method.POST) {
            request = asyncHttpClient.preparePost(httpApi.getUrl());
            request.setBody(httpApi.getBody());
        } else {
            throw new NotImplementedException();
        }

        addHeaders(httpApi, request);

        request.execute(new JobResponseHandler(jobInstance));
    }

    private void addHeaders(HttpApi httpApi, AsyncHttpClient.BoundRequestBuilder request) {
        String headers = httpApi.getHeaders();

        if(headers != null) {
            Arrays.stream(headers.split(";")).forEach(headerLine -> {
                String[] split = headerLine.split(":");
                String name = split[0].trim();
                String value = split[1].trim();

                request.setHeader(name, value);
            });
        }
    }

    @PreDestroy
    public void destroy() throws IOException, InterruptedException {
        asyncHttpClient.close();
        reporter.stop();
    }


    private class JobResponseHandler extends AsyncCompletionHandler<Object> implements AsyncHandlerExtensions {
        private final JobInstance jobInstance;

        public JobResponseHandler(JobInstance jobInstance) {
            this.jobInstance = jobInstance;
        }

        @Override
        public Object onCompleted(Response response) throws Exception {

            onJobComplete(jobInstance, response);
            return null;
        }

        @Override
        public void onThrowable(Throwable t){
            failedJobs.mark();
            Job job = jobInstance.getJob();
            log.warn("Failed to execute " + job.getName(), t);

            job.markAsNotExecuting();
        }

        @Override
        public void onOpenConnection() {

        }

        @Override
        public void onConnectionOpen() {
            connectionsOpen.inc();
        }

        @Override
        public void onPoolConnection() {

        }

        @Override
        public void onConnectionPooled() {

        }

        @Override
        public void onSendRequest(Object request) {
            onJobStarted(jobInstance);
        }

        @Override
        public void onRetry() {

        }

        @Override
        public void onDnsResolved(InetAddress address) {

        }

        @Override
        public void onSslHandshakeCompleted() {

        }

    }

    private void onJobStarted(JobInstance jobInstance) {
        long currentTime = new Date().getTime();
        jobInstance.setJobStartedTime(currentTime);

        jobsExecuting.mark();
        nioDelay.update(currentTime - jobInstance.getScheduledTime());
        totalDelay.update(currentTime - jobInstance.getTimeToRun());
    }

    private void onJobComplete(JobInstance jobInstance, Response response) {
        Job job = jobInstance.getJob();

        getResponseCodeMeter(response.getStatusCode()).mark();
        long executionTime = new Date().getTime() - jobInstance.getJobStartedTime();
        jobExecutionTime.update(executionTime);

        job.markAsNotExecuting();
        actOnStatusCode(job, response.getStatusCode());

        log.info("Finished executing {} in {} with response {} {} ",job.getName(),executionTime,response.getStatusCode(),response.getStatusText());
    }

    private void actOnStatusCode(Job job, int statusCode) {
      switch (statusCode) {
            /* TODO https://tools.ietf.org/html/rfc7231#section-6.5.4 states that 404 does not guarantee a permanent condition.
                Ideally, we should only sideline on 410
             */
            case 404 :
            case 410 :
                if(JobHelper.isScheduled(job)) {
                    ScheduledJob scheduledJob = (ScheduledJob) job;
                    scheduledJob.incrementNotFoundCount();
                }
                break;
        }
    }

    private Meter getResponseCodeMeter(int statusCode) {
        if(responseCodes.get(statusCode) == null) {
            responseCodes.put(statusCode, metricRegistry.meter("jobs.responseCode." + statusCode));
        }

        return responseCodes.get(statusCode);
    }
}
