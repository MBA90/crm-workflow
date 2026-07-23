package com.crm.workflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record WorkflowRequestStepDecisionRequest(
        @NotNull Boolean approved,
        @NotNull UUID decidedBy,
        @NotBlank String deciderName,
        String comment
) {
}
