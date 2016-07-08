package com.flipkart.kloud.httpjobscheduler;

public class Schedule {
    private Long startTime;

    private Long endTime;

    private Long repeatInterval;

    // For jackson, in case any client needs to de-serialize
    Schedule() {
    }

    public Schedule(Long startTime, Long endTime, Long repeatInterval) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.repeatInterval = repeatInterval;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(Long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }
}
