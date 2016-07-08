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

package com.flipkart.kloud.httpjobscheduler;

/**
 * @understands: One time job which gets deleted after execution
 */
public class OneTimeJob extends Job {
    private Long triggerTime;

    OneTimeJob() {
    }

    public OneTimeJob(String name, Long triggerTime, HttpApi httpApi) {
        super(name, httpApi);
        this.triggerTime = triggerTime;
    }


    public Long getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(Long triggerTime) {
        this.triggerTime = triggerTime;
    }

  }
