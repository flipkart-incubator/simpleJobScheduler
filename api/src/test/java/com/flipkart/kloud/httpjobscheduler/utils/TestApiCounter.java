package com.flipkart.kloud.httpjobscheduler.utils;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @understands Counts the hits on a given API
 */
@Component
public class TestApiCounter {

    private final Map<String, AtomicInteger> apiCountMap;

    public TestApiCounter() {
        apiCountMap = new HashMap<>();
    }

    public void incrementCount(String apiPath) {
        if(!this.apiCountMap.containsKey(apiPath)) {
            this.apiCountMap.put(apiPath,new AtomicInteger(0));
        }
        this.apiCountMap.get(apiPath).incrementAndGet();
    }

    public void reset() {
        apiCountMap.clear();
    }

    public void assertHeard(String apiPath, int count) {
        final AtomicInteger atomicCount = this.apiCountMap.get(apiPath);
        assertNotNull("Did not get any calls for " + apiPath,atomicCount);
        assertThat("Count mismatch for api " + apiPath, atomicCount.get(), is(count));
    }

    public void assertHeardAtleast(String apiPath, int count) {
        final AtomicInteger atomicCount = this.apiCountMap.get(apiPath);
        assertNotNull("Did not get any calls for " + apiPath,atomicCount);
        assertThat("Count for api "+ apiPath + "is lesser than expected ", atomicCount.get(), greaterThan(count));
    }
}
