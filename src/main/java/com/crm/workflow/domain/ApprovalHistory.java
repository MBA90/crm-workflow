package com.crm.workflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/** Immutable audit trail of request / request_step status transitions. */
@Entity
@Table(
        name = "approval_history",
        indexes = {
                @Index(name = "idx_approval_history_request", columnList = "request_id, occurred_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = "historyId")
public class ApprovalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "history_id", updatable = false, nullable = false)
    private UUID historyId;

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    /** nullable, request-level transitions have no step */
    @Column(name = "request_step_id")
    private UUID requestStepId;

    /** Keycloak sub */
    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    /** display snapshot */
    @Column(name = "actor_name", nullable = false)
    private String actorName;

    @Column(name = "from_status", length = 20)
    private String fromStatus;

    @Column(name = "to_status", nullable = false, length = 20)
    private String toStatus;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}
