package com.flipkart.kloud.httpjobscheduler.utils;

import com.flipkart.kloud.httpjobscheduler.*;

public class JobApiUtil {
    public static ScheduledJob createTestScheduledJob(String name, String url, long interval) {
        return new ScheduledJob(name, new HttpApi(HttpApi.Method.POST, url, "", null), new Schedule(System.currentTimeMillis(), TestConstants.fiveMinsFromNow(), interval));
    }

    public static OneTimeJob createTestOneTimeJob(String name, String url, long triggerTime) {
        return new OneTimeJob(name, triggerTime, new HttpApi(HttpApi.Method.POST, url, "", null));
    }
}
