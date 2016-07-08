package com.flipkart.kloud.httpjobscheduler;

import com.flipkart.kloud.httpjobscheduler.repositories.Repository;
import com.flipkart.kloud.httpjobscheduler.services.MasterJobScheduler;
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

    private static synchronized void setMaster(Boolean master) {
        isMaster = master;
    }

    @PreDestroy
    public void destroy() throws IOException, InterruptedException {
        leaderLatch.close();
        client.close();
    }
}
