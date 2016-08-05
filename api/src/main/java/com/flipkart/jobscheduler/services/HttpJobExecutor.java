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

import com.flipkart.jobscheduler.models.HttpApi;
import com.flipkart.jobscheduler.models.JobInstance;
import com.ning.http.client.AsyncHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HttpJobExecutor extends JobExecutor<HttpApi> implements DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(HttpJobExecutor.class);

    private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    public void destroy() throws IOException, InterruptedException {
        asyncHttpClient.close();
    }

    @Override
    protected void _executeAsync(HttpApi api, JobExecutionHandler jobExecutionHandler) {
        AsyncHttpClient.BoundRequestBuilder request = api.prepareRequest(asyncHttpClient);
        /*
            Todo: Presently, the responsibility of calling appropriate methods on JobExecutionHandler falls
            on the execution framework - like the AsyncHandlerImpl below. This should be handled by _this_
            framework going forward.
         */
        request.execute(new JobResponseHandler(jobExecutionHandler));
    }
}
