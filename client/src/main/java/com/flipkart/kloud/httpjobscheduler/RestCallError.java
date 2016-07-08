package com.flipkart.kloud.httpjobscheduler;

/**
 * Created by gautam on 16/8/15.
 */
public class RestCallError extends RuntimeException {
    private final int status;
    private final String body;

    public RestCallError(int status, String body) {
        super("Rest call failed with status " + status + " " + body);
        this.status = status;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }
}
