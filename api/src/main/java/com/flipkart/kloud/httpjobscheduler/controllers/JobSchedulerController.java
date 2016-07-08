package com.flipkart.kloud.httpjobscheduler.controllers;

import com.flipkart.kloud.httpjobscheduler.services.MasterJobScheduler;
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
