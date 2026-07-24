package com.crm.workflow.service;

import com.crm.workflow.domain.WorkflowDefinition;
import com.crm.workflow.domain.enums.EntityType;
import com.crm.workflow.domain.enums.WorkflowAction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowDefinitionService {

    WorkflowDefinition save(WorkflowDefinition definition);

    Optional<WorkflowDefinition> findById(UUID definitionId);

    Optional<WorkflowDefinition> findByEntityTypeAndActionAndVersion(EntityType entityType, WorkflowAction action, Integer version);

    Optional<WorkflowDefinition> findByEntityTypeAndActionAndActiveTrue(EntityType entityType, WorkflowAction action);

    Optional<WorkflowDefinition> findTopByEntityTypeAndActionOrderByVersionDesc(EntityType entityType, WorkflowAction action);

    List<WorkflowDefinition> findByEntityTypeAndActionOrderByVersionDesc(EntityType entityType, WorkflowAction action);
}
