package com.rmacd.rundeck.plugins;

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.plugins.PluginLogger;
import org.apache.curator.framework.api.GetDataBuilder;

import java.util.List;

/**
 * returns only a subset of the methods that we are interested in
 */
public interface RDZKClient {
    /**
     * Returns a list of resources stored by the rundeck plugin
     * @return
     */
    List<String> getResources() throws StepException;

    void lockResource(String resourceId, Integer timeout, PluginLogger logger) throws StepException;

    void unlockResource(String resourceName, PluginLogger logger) throws StepException;

    boolean resourceExists(String resourceName) throws StepException;

    void addResource(String resourceId) throws StepException;
}
