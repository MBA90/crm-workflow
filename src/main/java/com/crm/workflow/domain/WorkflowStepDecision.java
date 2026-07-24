package com.crm.workflow.domain;

import com.crm.workflow.domain.enums.DecisionOutcome;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/** One vote per approver for a parallel / quorum workflow_request_step. */
@Entity
@Table(
        name = "workflow_step_decisions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_workflow_step_decisions_step_decider",
                columnNames = {"request_step_id", "decided_by"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = "decisionId")
public class WorkflowStepDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "decision_id", updatable = false, nullable = false)
    private UUID decisionId;

    @Column(name = "request_step_id", nullable = false)
    private UUID requestStepId;

    /** Keycloak sub */
    @Column(name = "decided_by", nullable = false)
    private UUID decidedBy;

    /** display snapshot */
    @Column(name = "decider_name", nullable = false)
    private String deciderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 20)
    private DecisionOutcome decision;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "decided_at", nullable = false)
    private Instant decidedAt;
}
