package com.flipkart.kloud.httpjobscheduler;

/**
 * Common attributes of job
 */
public abstract class Job {
    private String name;

    private HttpApi httpApi;

    // For jackson, in case any client needs to de-serialize
    Job() {
    }

    public Job(String name, HttpApi httpApi) {
        this.name = name;
        this.httpApi = httpApi;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HttpApi getHttpApi() {
        return httpApi;
    }

    public void setHttpApi(HttpApi httpApi) {
        this.httpApi = httpApi;
    }
}
