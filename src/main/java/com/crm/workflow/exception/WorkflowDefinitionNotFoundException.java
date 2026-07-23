package com.crm.workflow.exception;

import com.crm.workflow.domain.enums.EntityType;
import com.crm.workflow.domain.enums.WorkflowAction;

import java.util.UUID;

public class WorkflowDefinitionNotFoundException extends RuntimeException {

    public WorkflowDefinitionNotFoundException(UUID definitionId) {
        super("Workflow definition not found: " + definitionId);
    }

    public WorkflowDefinitionNotFoundException(EntityType entityType, WorkflowAction action) {
        super("No active workflow definition found for entityType=" + entityType + ", action=" + action);
    }
}
