package com.flipkart.kloud.httpjobscheduler.controllers;


import com.flipkart.kloud.httpjobscheduler.Application;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/health")
public class HealthController {
    @RequestMapping(value = "/inRotation", method = RequestMethod.GET)
    public void isInRotation(HttpServletResponse response) {
        if(Application.getIsMaster()) {
            response.setStatus(200);
        } else {
            response.setStatus(404);
        }
    }

}
