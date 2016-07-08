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
