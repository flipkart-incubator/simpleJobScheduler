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

package com.flipkart.jobscheduler.utils;

import com.flipkart.jobscheduler.models.JobInstance;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * @understands Matches job instances based only on the job that it contains
 */
public class JobInstanceMatcher extends TypeSafeMatcher<JobInstance> {

    private final JobInstance toCompare;

    public static JobInstanceMatcher matchJob(JobInstance jobInstance) {
        return new JobInstanceMatcher(jobInstance);
    }

    private JobInstanceMatcher(JobInstance toCompare) {
        this.toCompare = toCompare;
    }

    @Override
    protected boolean matchesSafely(JobInstance item) {
        return this.toCompare.getJob().equals(item.getJob());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("jobInstance(" + toCompare + ") has job " + toCompare.getJob());
    }
}
