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

package com.flipkart.kloud.httpjobscheduler.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;

    @NotNull
    @JsonProperty
    private Long startTime;

    @JsonProperty
    private Long endTime;

    @JsonProperty
    private Long repeatInterval;

    // For Jackson
    Schedule() {
        this.startTime = new Date().getTime();
        this.endTime = null;
        this.repeatInterval = null;
    }

    public Schedule(Long startTime, Long endTime, Long repeatInterval) {
        this();
        this.endTime = endTime;
        this.repeatInterval = repeatInterval;
        this.startTime = startTime;
    }

    public Schedule(Long startTime) {
        this();
        this.startTime = startTime;
    }

    public Long nextRunTime() {
        Long currentTime = new Date().getTime();

        if(endTime != null && currentTime > endTime) {
            return null;
        } else if(currentTime < startTime) {
            return startTime;
        }
        return currentTime - (currentTime - startTime)% repeatInterval + repeatInterval;
    }

}
