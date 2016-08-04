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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @understands:  parent entity for ScheduledJob and OneTimeJob
 */

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name="job", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public abstract class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    @NotBlank
    @Column(unique = true)
    @JsonProperty
    protected String name;

    @Transient
    @JsonIgnore
    protected boolean isExecuting;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "api_id")
    @NotNull
    @JsonProperty
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME,property = "type")
    @JsonSubTypes(value = {@JsonSubTypes.Type(value = HttpApi.class,name = "http")})
    protected Api api;

    @Transient
    @JsonIgnore
    private int numRuns;

    public Job() {
        this.isExecuting = false;
        this.numRuns = 0;
    }

    public Job(String name, HttpApi api) {
        this();
        this.name = name;
        this.api = api;
    }

    public String getName() {
        return name;
    }

    public Api getApi() {
        return api;
    }

    public synchronized void markAsNotExecuting() {
        isExecuting = false;
    }

    public synchronized boolean markAsExecuting() {

        if(!isExecuting) {
            numRuns += 1;
            isExecuting = true;
            return true;
        }
        return false;
    }

    public boolean sameNameAs(String jobName) {
        return this.name.equals(jobName);
    }

    public abstract JobInstance nextInstance();

    public abstract boolean shouldBeSidelined();

    public abstract boolean isSidelined();
}
