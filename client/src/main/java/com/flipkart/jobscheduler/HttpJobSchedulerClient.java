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

package com.flipkart.jobscheduler;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class HttpJobSchedulerClient {
    private String jobSchedulerUrl;

    public HttpJobSchedulerClient(String jobSchedulerUrl) {
        this.jobSchedulerUrl = jobSchedulerUrl;
    }

    /**
     * creates scheduled job
     * @param job
     */
    public void createJob(ScheduledJob job) {
        try {
            HttpResponse<String> response = Unirest.post(jobSchedulerUrl + "/jobs/scheduled")
                    .header("Content-Type", "application/json")
                    .body(new Gson().toJson(job))
                    .asString();

            if(!isSuccessful(response)) {
                throw new RestCallError(response.getStatus(), response.getBody());
            }

        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * creates one time job
     * @param job
     */
    public void createJob(OneTimeJob job) {
        try {
            HttpResponse<String> response = Unirest.post(jobSchedulerUrl + "/jobs/oneTime")
                    .header("Content-Type", "application/json")
                    .body(new Gson().toJson(job))
                    .asString();

            if(!isSuccessful(response)) {
                throw new RestCallError(response.getStatus(), response.getBody());
            }

        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeJob(String name) {
        try {
            HttpResponse<String> response = Unirest.delete(jobSchedulerUrl + "/jobs?name=" + name)
                    .header("Content-Type", "application/json")
                    .asString();

            if(!isSuccessful(response)) {
                throw new RestCallError(response.getStatus(), response.getBody());
            }

        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isSuccessful(HttpResponse<String> response) {
        return response.getStatus() >= 200 && response.getStatus() < 300;
    }
}
