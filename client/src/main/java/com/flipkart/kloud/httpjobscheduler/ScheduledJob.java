package com.flipkart.kloud.httpjobscheduler;


/**
 * @understands: Scheduled job
 */
public class ScheduledJob  extends Job {
    private Schedule schedule;

    ScheduledJob(){}

    public ScheduledJob(String name, HttpApi httpApi, Schedule schedule) {
        super(name, httpApi);
        this.schedule = schedule;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }
}
