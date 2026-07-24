package com.crm.workflow.dto;

import com.crm.workflow.domain.enums.DecisionOutcome;

import java.time.Instant;
import java.util.UUID;

public record WorkflowStepDecisionDto(
        UUID decisionId,
        UUID requestStepId,
        UUID decidedBy,
        String deciderName,
        DecisionOutcome decision,
        String comment,
        Instant decidedAt
) {
}
