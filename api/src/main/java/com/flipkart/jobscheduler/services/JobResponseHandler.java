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

import com.codahale.metrics.SharedMetricRegistries;
import com.flipkart.jobscheduler.services.JobExecutionHandler.JobStatus;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHandlerExtensions;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

import static com.flipkart.jobscheduler.util.Constants.MAIN_METRIC_REGISTRY;

/**
 * A Handler created per <code>Api</code> invocation
 * The invocation being asynchronous in nature, the JobResponseHandler has methods that are invoked at various stages of
 * <code>Api</code> execution.
 */
public class JobResponseHandler extends AsyncCompletionHandler<Object> implements AsyncHandlerExtensions {
    private final JobExecutionHandler jobExecutionHandler;
    private static final Logger log = LoggerFactory.getLogger(JobExecutor.class);

    public JobResponseHandler(JobExecutionHandler jobExecutionHandler) {
        this.jobExecutionHandler = jobExecutionHandler;
    }

    @Override
    public Object onCompleted(Response response) throws Exception {
        jobExecutionHandler.onJobCompleted(getJobStatus(response.getStatusCode()));
        return null;
    }

    @Override
    public void onThrowable(Throwable t){
        jobExecutionHandler.onException(t);
    }

    @Override
    public void onOpenConnection() {

    }

    @Override
    public void onConnectionOpen() {
        SharedMetricRegistries.getOrCreate(MAIN_METRIC_REGISTRY).counter("connections.open").inc();
    }

    @Override
    public void onPoolConnection() {

    }

    @Override
    public void onConnectionPooled() {

    }

    @Override
    public void onSendRequest(Object request) {
        jobExecutionHandler.onJobStarted();
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

    private JobStatus getJobStatus(int statusCode) {
        switch (statusCode) {
            /* TODO https://tools.ietf.org/html/rfc7231#section-6.5.4 states that 404 does not guarantee a permanent condition.
                Ideally, we should only sideline on 410
             */
            case 404 :
            case 410 :
                return JobStatus.PERMANENT_FAILURE;
            default:
                return JobStatus.SUCCESS;
        }
    }
}
