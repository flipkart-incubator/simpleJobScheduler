/*
 * Copyright 2012-2016, the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.flipkart.jobscheduler;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.flipkart.jobscheduler.repositories.Repository;
import com.flipkart.jobscheduler.services.MasterJobScheduler;
import com.flipkart.jobscheduler.util.Constants;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.lang.management.ManagementFactory;

@SpringBootApplication
public class Application implements CommandLineRunner {
    public static final Logger log = LoggerFactory.getLogger(Application.class);

    public static boolean isMaster = false;

    @Autowired
    private Repository repository;

    @Autowired
    private MasterJobScheduler masterJobScheduler;

    @Value("${zookeeperConnString}")
    private String zookeeperConnString;

    @Autowired
    private MetricRegistry metricRegistry;

    private JmxReporter reporter;
    private LeaderLatch leaderLatch;
    private CuratorFramework client;

    public static void main(String args[]) throws InterruptedException {
        SpringApplication.run(Application.class);
    }

    public static boolean getIsMaster() {
        return isMaster;
    }

    @Override
    public void run(String... strings) throws Exception {
        startReporting();
        client = CuratorFrameworkFactory.newClient(zookeeperConnString,
                new ExponentialBackoffRetry(1000, Integer.MAX_VALUE));
        client.start();
        client.getZookeeperClient().blockUntilConnectedOrTimedOut();

        leaderLatch = new LeaderLatch(client, "/http-job-scheduler/leader",
                ManagementFactory.getRuntimeMXBean().getName());

        leaderLatch.addListener(new LeaderLatchListener() {
            @Override
            public void isLeader() {
                setMaster(true);
                masterJobScheduler.resume();
            }

            @Override
            public void notLeader() {
                setMaster(false);
                masterJobScheduler.pause();
            }
        });
        leaderLatch.start();
    }

    private void startReporting() {
        reporter = JmxReporter.forRegistry(metricRegistry).build();
        SharedMetricRegistries.add(Constants.MAIN_METRIC_REGISTRY,metricRegistry);
        reporter.start();
    }

    private static synchronized void setMaster(Boolean master) {
        isMaster = master;
    }

    @PreDestroy
    public void destroy() throws IOException, InterruptedException {
        reporter.close();
        leaderLatch.close();
        client.close();
    }
}
