package com.flipkart.kloud.httpjobscheduler.utils;

public class TestConstants {
    public static final long FIVE_MINS_MILLIS = 300000l;
    public static final long ONE_SECOND = 1000l;
    public static long fiveMinsFromNow() {
        return System.currentTimeMillis() + FIVE_MINS_MILLIS;
    }

    public static long oneSecondFromNow() { return System.currentTimeMillis() + ONE_SECOND; }

    public static long oneMinBack() {
        return System.currentTimeMillis() - 60000l;
    }
}
