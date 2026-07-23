package com.crm.workflow.dto.request;

import com.crm.workflow.domain.enums.WorkflowAction;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record WorkflowRequestCreateRequest(
        @NotNull UUID definitionId,
        @NotBlank String entityType,
        UUID entityId,
        @NotNull WorkflowAction action,
        @NotNull JsonNode payload,
        @NotNull UUID requestedBy,
        @NotBlank String requesterName
) {
}
