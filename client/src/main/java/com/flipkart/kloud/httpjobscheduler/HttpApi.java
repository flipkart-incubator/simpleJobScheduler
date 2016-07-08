package com.flipkart.kloud.httpjobscheduler;

public class HttpApi {
    public enum Method {
        GET, POST;

    }
    private HttpApi.Method method;

    private String url;

    private String body;

    private String headers;

    // For Jackson, in case any client needs to de-serialize
    HttpApi() {
    }

    public HttpApi(Method method, String url, String body, String headers) {
        this.method = method;
        this.url = url;
        this.body = body;
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }
}
