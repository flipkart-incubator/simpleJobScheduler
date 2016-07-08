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

package com.flipkart.jobscheduler.controllers;

import com.flipkart.jobscheduler.services.MasterJobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/jobScheduler")
public class JobSchedulerController {
    @Autowired
    private MasterJobScheduler masterJobScheduler;

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/pause", method = RequestMethod.PUT)
    public void pause() {
        masterJobScheduler.pause();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/resume", method = RequestMethod.PUT)
    public void resume() {
        masterJobScheduler.resume();
    }
}
