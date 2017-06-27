package com.rmacd.rundeck.plugins;

import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;

enum StepExceptionsEnum implements FailureReason {
    ERROR_ACQUIRE_LOCK,
    ERROR_RELEASE_LOCK,
    ERROR_ZK_CONNECTION,
    ERROR_RESOURCE_NOT_EXIST,
    ERROR_CREATE_RESOURCE,
    ERROR_LOCKTYPE_NOT_SPECIFIED
}
