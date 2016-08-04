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

package com.flipkart.jobscheduler.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.jobscheduler.services.JobResponseHandler;
import com.ning.http.client.AsyncHttpClient;
import org.hibernate.validator.constraints.NotBlank;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Arrays;

@Entity
public class HttpApi extends Api {
    public enum Method {
        GET, POST;
    }
    @NotNull
    @Enumerated(value = EnumType.STRING)
    @JsonProperty
    private HttpApi.Method method;

    @Lob
    @JsonProperty
    private String body;

    @Lob
    @JsonProperty
    private String headers;

    HttpApi() {}

    public HttpApi(String url, Method method) {
        this(url, method, null);
    }

    public HttpApi(String url, Method method, String headers) {
        this.url = url;
        this.method = method;
        this.headers = headers;
    }

    @Override
    public void executeAsync(AsyncHttpClient asyncHttpClient, JobResponseHandler jobResponseHandler) {
        AsyncHttpClient.BoundRequestBuilder request;

        if (method == HttpApi.Method.GET) {
            request = asyncHttpClient.prepareGet(this.url);
        } else if (method == HttpApi.Method.POST) {
            request = asyncHttpClient.preparePost(this.url);
            request.setBody(this.body);
        } else {
            throw new NotImplementedException();
        }

        addHeaders(request);

        request.execute(jobResponseHandler);
    }
    private void addHeaders(AsyncHttpClient.BoundRequestBuilder request) {

        if(headers != null) {
            Arrays.stream(headers.split(";")).forEach(headerLine -> {
                String[] split = headerLine.split(":");
                String name = split[0].trim();
                String value = split[1].trim();

                request.setHeader(name, value);
            });
        }
    }

}
