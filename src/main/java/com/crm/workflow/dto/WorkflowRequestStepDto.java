package com.crm.workflow.dto;

import com.crm.workflow.domain.enums.ApprovalType;
import com.crm.workflow.domain.enums.RequestStepStatus;

import java.time.Instant;
import java.util.UUID;

public record WorkflowRequestStepDto(
        UUID requestStepId,
        Integer stepOrder,
        String stepName,
        String approverRole,
        ApprovalType approvalType,
        Integer quorumCount,
        UUID assignedTo,
        UUID delegateTo,
        RequestStepStatus status,
        UUID decidedBy,
        String deciderName,
        String comment,
        Instant slaDueAt,
        Instant escalatedAt,
        Instant decidedAt
) {
}
