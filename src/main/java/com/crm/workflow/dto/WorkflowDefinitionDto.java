package com.crm.workflow.dto;

import com.crm.workflow.domain.enums.EntityType;
import com.crm.workflow.domain.enums.WorkflowAction;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkflowDefinitionDto(
        UUID definitionId,
        EntityType entityType,
        WorkflowAction action,
        String name,
        Integer version,
        boolean active,
        Instant createdAt,
        List<WorkflowDefinitionStepDto> steps
) {
}
