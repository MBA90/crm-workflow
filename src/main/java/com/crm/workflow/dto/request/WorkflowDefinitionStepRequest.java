package com.crm.workflow.dto.request;

import com.crm.workflow.domain.enums.ApprovalType;
import com.crm.workflow.domain.enums.OnRejectAction;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WorkflowDefinitionStepRequest(
        @NotNull @Min(1) Integer stepOrder,
        @NotBlank String stepName,
        @NotBlank String approverRole,
        @NotNull ApprovalType approvalType,
        @Min(1) Integer quorumCount,
        JsonNode condition,
        @NotNull @Min(1) Integer slaHours,
        @NotNull OnRejectAction onReject,
        @Min(1) Integer returnToStep
) {
}
