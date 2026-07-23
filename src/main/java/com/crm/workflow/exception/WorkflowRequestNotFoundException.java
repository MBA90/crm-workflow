package com.crm.workflow.exception;

import java.util.UUID;

public class WorkflowRequestNotFoundException extends RuntimeException {

    public WorkflowRequestNotFoundException(UUID requestId) {
        super("Workflow request not found: " + requestId);
    }
}
