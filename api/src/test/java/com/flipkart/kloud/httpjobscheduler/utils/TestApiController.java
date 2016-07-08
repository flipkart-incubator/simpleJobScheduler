package com.flipkart.kloud.httpjobscheduler.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/test")
public class TestApiController {

    @Autowired
    private TestApiCounter testApiCounter;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void dummyApi() {
        testApiCounter.incrementCount("test");
    }

    @RequestMapping(value = "/404",method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void dummy404Api() {
        testApiCounter.incrementCount("test404");
    }
}
