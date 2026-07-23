package com.crm.workflow.dto;

import com.crm.workflow.domain.enums.OverallStatus;
import com.crm.workflow.domain.enums.WorkflowAction;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkflowRequestDto(
        UUID requestId,
        UUID definitionId,
        String entityType,
        UUID entityId,
        WorkflowAction action,
        JsonNode payload,
        OverallStatus overallStatus,
        Integer currentStep,
        UUID requestedBy,
        String requesterName,
        Instant createdAt,
        Instant completedAt,
        List<WorkflowRequestStepDto> steps
) {
}
