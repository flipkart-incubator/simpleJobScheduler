package com.flipkart.kloud.httpjobscheduler;

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
