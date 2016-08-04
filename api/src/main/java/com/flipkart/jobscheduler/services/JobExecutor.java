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
import org.springframework.beans.factory.DisposableBean;
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
public class JobExecutor  implements DisposableBean {
    public static final Logger log = LoggerFactory.getLogger(JobExecutor.class);

    private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
    @Autowired
    private MetricRegistry metricRegistry;

    public void executeAsync(JobInstance jobInstance) {
        jobInstance.setScheduledTime(new Date().getTime());
        Job job = jobInstance.getJob();

        log.info("Executing {} delay {}", job.getName(), new Date().getTime() - jobInstance.getTimeToRun());

        if(job.markAsExecuting()) {
            job.getApi().executeAsync(asyncHttpClient, new JobResponseHandler(jobInstance));
        } else {
            log.info("Job {} is already running ");
        }
    }

    public void destroy() throws IOException, InterruptedException {
        asyncHttpClient.close();
    }



}
