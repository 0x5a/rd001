package com.rmacd.rundeck.plugins;

import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkImpl;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.internal.runners.statements.Fail;

import java.util.concurrent.TimeUnit;

/**
 * Created by ronald on 31/05/2017.
 */
public class ZKClientManager {


    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", retryPolicy);

    ZKClientManager() {
        client.start();
    }

    public void runUpd() {

        InterProcessSemaphoreMutex mutex = new InterProcessSemaphoreMutex(
            client, "/test01-lock"
        );

        try {
            mutex.acquire();
        } catch (Exception e) {
            System.out.println("threw bad exception: " + e.getCause());
            e.printStackTrace();
        }

        System.out.println("mutex should have been acquired, retrying mutex");

        try {
            int i = 0;
            while (! mutex.acquire(5, TimeUnit.SECONDS) && i < 3) {
                System.out.println("waiting another 5 seconds ...");
                i++;
            }
        } catch (Exception e) {
            System.out.println("threw exception");
        }

        System.out.println("now releasing ...");

        try {
            mutex.release();
        } catch (Exception e) {
            System.out.println("exception in releasing: " + e.getCause());
            e.printStackTrace();
        }

        System.out.println("reached end");

//        (new Thread(new ZkThread())).getState();
    }

    class ZkThread implements Runnable {

        @Override
        public void run() {

        }
    }

}
