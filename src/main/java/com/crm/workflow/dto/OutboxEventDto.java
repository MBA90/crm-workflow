package com.crm.workflow.dto;

import com.crm.workflow.domain.enums.OutboxEventType;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record OutboxEventDto(
        UUID eventId,
        UUID aggregateId,
        OutboxEventType eventType,
        JsonNode payload,
        boolean published,
        Instant createdAt,
        Instant publishedAt
) {
}
