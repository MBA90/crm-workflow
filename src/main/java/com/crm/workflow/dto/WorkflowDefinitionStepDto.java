package com.crm.workflow.dto;

import com.crm.workflow.domain.enums.ApprovalType;
import com.crm.workflow.domain.enums.OnRejectAction;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public record WorkflowDefinitionStepDto(
        UUID stepId,
        Integer stepOrder,
        String stepName,
        String approverRole,
        ApprovalType approvalType,
        Integer quorumCount,
        JsonNode condition,
        Integer slaHours,
        OnRejectAction onReject,
        Integer returnToStep
) {
}
