package com.crm.workflow.service;

import com.crm.workflow.domain.enums.EntityType;
import com.crm.workflow.domain.enums.WorkflowAction;
import com.crm.workflow.dto.request.WorkflowDefinitionCreateRequest;
import com.crm.workflow.dto.WorkflowDefinitionDto;

import java.util.List;
import java.util.UUID;

public interface WorkflowDefinitionServiceFacade {

    WorkflowDefinitionDto create(WorkflowDefinitionCreateRequest request);

    WorkflowDefinitionDto getById(UUID definitionId);

    List<WorkflowDefinitionDto> list(EntityType entityType, WorkflowAction action);

    WorkflowDefinitionDto getActive(EntityType entityType, WorkflowAction action);

    WorkflowDefinitionDto activate(UUID definitionId);
}
