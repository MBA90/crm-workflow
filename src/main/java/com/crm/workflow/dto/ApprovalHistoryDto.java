package com.crm.workflow.dto;

import java.time.Instant;
import java.util.UUID;

public record ApprovalHistoryDto(
        UUID historyId,
        UUID requestId,
        UUID requestStepId,
        UUID actorId,
        String actorName,
        String fromStatus,
        String toStatus,
        String comment,
        Instant occurredAt
) {
}
