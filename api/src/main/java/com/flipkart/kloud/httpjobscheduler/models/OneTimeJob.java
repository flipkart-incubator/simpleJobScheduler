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

/**
 * @understands: One time job which is deleted after execution
 */

@Entity
public class OneTimeJob extends Job {
    @JsonProperty
    private Long triggerTime;

    OneTimeJob() {}

    public OneTimeJob(String name, HttpApi httpApi, Long triggerTime) {
        super(name, httpApi);
        this.triggerTime = triggerTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OneTimeJob that = (OneTimeJob) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "OneTimeJob{" +
                "id=" + id +
                ", name='" + name + "\'" +
                '}';
    }

    @Override
    public JobInstance nextInstance() {
        if(triggerTime > System.currentTimeMillis()) {
            return new JobInstance(this,triggerTime);
        }
        return null;
    }

    @Override
    @JsonIgnore
    public boolean shouldBeSidelined() {
        return false;
    }

    @Override
    @JsonIgnore
    public boolean isSidelined() {
        return false;
    }
}