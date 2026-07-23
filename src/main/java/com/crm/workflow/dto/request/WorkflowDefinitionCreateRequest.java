package com.crm.workflow.dto.request;

import com.crm.workflow.domain.enums.EntityType;
import com.crm.workflow.domain.enums.WorkflowAction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record WorkflowDefinitionCreateRequest(
        @NotNull EntityType entityType,
        @NotNull WorkflowAction action,
        @NotBlank String name,
        @NotEmpty @Valid List<WorkflowDefinitionStepRequest> steps
) {
}
