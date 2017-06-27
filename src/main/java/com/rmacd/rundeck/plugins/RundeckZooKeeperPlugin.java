package com.rmacd.rundeck.plugins;

import com.dtolabs.rundeck.core.execution.ExecutionContext;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyValidator;
import com.dtolabs.rundeck.core.plugins.configuration.ValidationException;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import com.dtolabs.rundeck.plugins.util.PropertyBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Plugin(name = RundeckZooKeeperPlugin.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowStep)
public class RundeckZooKeeperPlugin implements StepPlugin, Describable {

    public static final String SERVICE_PROVIDER_NAME = "com.rmacd.rd-plugins.RundeckZooKeeperPlugin";

    RDZKClientImpl client = new RDZKClientImpl();

    PluginConf conf;
    {
        conf = PluginConfImpl.INSTANCE;
    }

    /*
     * Private method to check validity of the resource lock timeout
     */
    private PropertyValidator timeoutPropertyValidator = new PropertyValidator() {
        @Override
        public boolean isValid(String value) throws ValidationException {
        Integer n = Integer.getInteger(value);
        return n >= 0;
        }
    };

    /**
     * Displayed by the Rundeck frond end
     * @return Rundeck Description
     */
    public Description getDescription() {
        List<String> resourceList = new ArrayList<>();
        try {
            resourceList = client.getResources();
        } catch (StepException e) {
            resourceList.add("-- PLUGIN ERROR: Could not connect to resource server --");
        }

        if (resourceList.size() < 1) {
            resourceList.add("-- PLUGIN ERROR: No Rundeck resources defined");
        }

        return DescriptionBuilder.builder()
            .name(SERVICE_PROVIDER_NAME)
            .title("ZooKeeper Resource Lock")
            .description("Provides a semaphore mechanism for resources accessed by RunDeck")
            .property(PropertyBuilder.builder()
                .select("lockType")
                .title("Lock type")
                .description("Specify whether this step is to lock or to unlock a resource")
                .required(true)
                .values("lock", "unlock")
                .build()
            )
            .property(PropertyBuilder.builder()
                .select("resourceId")
                .title("Resource ID")
                .description("Select the Resource Name / ID as held by ZooKeeper")
                .required(true)
                .values(resourceList)
                .build()
            )
            .property(PropertyBuilder.builder()
                .integer("timeoutSec")
                .title("Plugin Timeout")
                .description("Wait until max timeout (specified in seconds, defaults " +
                    "to five minutes) to lock a resource. " +
                    "Halt the job and return as failure if timeout is reached. " +
                    "Specify a zero for no timeout.")
                .validator(timeoutPropertyValidator)
                .required(true)
                .defaultValue(conf.getStr(PluginConfImpl.Key.LOCK_DEFAULT_TIMEOUT))
                .build()
            )
            .build();
    }

    /**
     * The {@link PluginStepContext} provides access to the appropriate Nodes, the configuration of the plugin, and
     * details about the step number and context.
     */
    public void executeStep(final PluginStepContext context,
                            final Map<String, Object> configuration) throws StepException {

        InstanceParameters params = new InstanceParameters();

        switch (String.valueOf(configuration.get("lockType"))) {
            case "lock":
                params.lockType = InstanceParameters.LockType.LOCK;
                break;
            case "unlock":
                params.lockType = InstanceParameters.LockType.UNLOCK;
                break;
            default:
                throw new StepException("Lock type must be specified", StepExceptionsEnum.ERROR_LOCKTYPE_NOT_SPECIFIED);
        }

        // check ahead and see that an unlock exists for this resource
        ExecutionContext e = context.getExecutionContext();
        PluginLogger logger = context.getLogger();

        String resourceId = (String) configuration.get("resourceId");
        logger.log(5, String.format("Got resource ID of '%s'", resourceId));

        if (! client.resourceExists(resourceId)) {
            throw new StepException(
                "Resource %s does not exist, bailing ... ",
                StepExceptionsEnum.ERROR_RESOURCE_NOT_EXIST
            );
        }

//        logger.log(1, "message");
//
//        System.out.println("Example step executing on nodes: " + context.getNodes().getNodeNames());
//        System.out.println("Example step configuration: " + configuration);
//        System.out.println("Example step num: " + context.getStepNumber());
//        System.out.println("Example step context: " + context.getStepContext());

        // The timeoutSecS option is passed by the Rundeck front end ... if this option is
        // missing, we can instead grab the option from the properties file.
        String timeoutSecS = (String) configuration.get("timeoutSec");

        // load up default from properties in case parse fails
        Integer timeoutSecI = conf.getInt(PluginConfImpl.Key.LOCK_DEFAULT_TIMEOUT);
        if (null != timeoutSecS && ! timeoutSecS.isEmpty()) {
            timeoutSecI = Integer.getInteger(timeoutSecS);
        }

        switch (params.lockType) {
            case LOCK:
                logger.log(4, String.format(
                    "About to request lock on resource %s", configuration.get("resourceId")
                ));
                client.lockResource(resourceId, timeoutSecI, logger);
                break;
            case UNLOCK:
                logger.log(4, String.format(
                    "About to request unlock on resource %s", configuration.get("resourceId")
                ));
                client.unlockResource(resourceId, logger);
                break;
            default:
                throw new StepException("Could not determine lock type", StepExceptionsEnum.ERROR_LOCKTYPE_NOT_SPECIFIED);
        }
    }

    static class InstanceParameters {
        public enum LockType {
            LOCK, UNLOCK;
        }
        LockType lockType;
    }

}
