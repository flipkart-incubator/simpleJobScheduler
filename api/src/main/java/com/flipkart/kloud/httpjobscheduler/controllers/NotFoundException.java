package com.flipkart.kloud.httpjobscheduler.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by gautam on 6/8/15.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    public NotFoundException(String resourceType, String resourceId) {
        super("Could not find "  + resourceType + " with id " + resourceId);
    }
}
