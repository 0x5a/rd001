package com.rmacd.rundeck.plugins;

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.plugins.PluginLogger;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by ronald on 04/06/2017.
 */
public class RDZKClientImpl implements RDZKClient {

    PluginConf conf;
    CuratorFramework client;
    {
        conf = PluginConfImpl.INSTANCE;

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient(
                String.format("%s:%s",
                        conf.getStr(PluginConfImpl.Key.ZK_HOST),
                        conf.getStr(PluginConfImpl.Key.ZK_PORT)
                ), retryPolicy);

        client.start();
//        try {
//            client.blockUntilConnected(10, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            throw new
//            throw new StepException(
//                "Could not connect to ZooKeeper instance after 10 seconds, giving up.",
//                FailureReasons.ERROR_ZK_CONNECTION
//            );
//        }
    }

    @Override
    public List<String> getResources() throws StepException {
        List<String> resources = new ArrayList<>();

        try {
            // we're catching an interrupted exception as well as forcing
            // an exception if the blockUntilConnected() method returns false
            if (!client.blockUntilConnected(3, TimeUnit.SECONDS)) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new StepException("Could not connect to resource manager", StepExceptionsEnum.ERROR_ZK_CONNECTION);
        }

        try {
            resources.addAll(client.getChildren().forPath(conf.getStr(PluginConfImpl.Key.ZK_RESOURCE_PATH)));
        } catch (Exception e) {
            resources.add("Connected, but no resources available");
        }

        return resources;
    }

    @Override
    public void lockResource(String resourceName, Integer timeout, PluginLogger logger) throws StepException {
//        logger.log(3, String.format("Checking whether resource %s exists", resourceName));
//        if (!resourceExists(resourceName)) {
//            throw new StepException(
//                String.format("Resource %s does not exist", resourceName),
//                FailureReasons.ERROR_RESOURCE_NOT_EXIST
//            );
//        }

        String resourcePath = String.format(conf.getStr(PluginConfImpl.Key.ZK_RESOURCE_PATH), resourceName);

        InterProcessSemaphoreMutex mutex = new InterProcessSemaphoreMutex(
            client, resourcePath
        );
        try {
            int i = 0;
            do {
                logger.log(2, String.format("Attempting to lock resource %s", resourcePath));
                boolean locked = mutex.acquire(10, TimeUnit.SECONDS);
                i += 10;
                logger.log(3, locked ?
                    String.format("Acquired lock on resource %s", resourcePath) :
                    String.format("Did not acquire lock (%d of %d seconds)", i, timeout
                ));
            } while (i < timeout);
        } catch (Exception e) {
            logger.log(1, String.format("Could not acquire exclusive lock on resource %s", resourceName));
            throw new StepException(
                String.format("Could not acquire exclusive lock on resource %s", resourceName),
                StepExceptionsEnum.ERROR_ACQUIRE_LOCK
            );
        }
    }

    @Override
    public void unlockResource(String resourceName, PluginLogger logger) throws StepException {
        String resourcePath = String.format(conf.getStr(PluginConfImpl.Key.ZK_RESOURCE_PATH), resourceName);
        InterProcessSemaphoreMutex mutex = new InterProcessSemaphoreMutex(
            client, resourcePath
        );
        try {
            logger.log(2, String.format("Attempting to release lock on %s", resourcePath));
            mutex.release();
        } catch (Exception e) {
            logger.log(1, String.format("Could not release lock on %s", resourcePath));
            throw new StepException(
                String.format("Could not release lock on resource %s", resourcePath),
                StepExceptionsEnum.ERROR_RELEASE_LOCK
            );
        }
    }

    @Override
    public boolean resourceExists(String resourceName) throws StepException {
        String resourcePath = String.format(conf.getStr(PluginConfImpl.Key.ZK_RESOURCE_PATH), resourceName);
        List<String> availableResources = getResources();
        for (String availableResource : availableResources) {
            if (resourcePath.equals(availableResource)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addResource(String resourceName) throws StepException {
        try {
            client.create().creatingParentContainersIfNeeded().forPath(String.format("%s/%s", conf.getStr(PluginConfImpl.Key.ZK_RESOURCE_PATH), resourceName));
        } catch (Exception e) {
            throw new StepException("Could not add resource", StepExceptionsEnum.ERROR_CREATE_RESOURCE);
        }
    }
}
