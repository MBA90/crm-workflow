package com.crm.workflow.exception;

import java.util.UUID;

public class WorkflowRequestStepNotFoundException extends RuntimeException {

    public WorkflowRequestStepNotFoundException(UUID requestStepId) {
        super("Workflow request step not found: " + requestStepId);
    }
}
