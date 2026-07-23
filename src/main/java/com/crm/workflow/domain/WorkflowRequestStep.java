package com.crm.workflow.domain;

import com.crm.workflow.domain.enums.ApprovalType;
import com.crm.workflow.domain.enums.RequestStepStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Entity
@Table(
        name = "workflow_request_steps",
        indexes = {
                @Index(name = "idx_workflow_request_steps_request_order", columnList = "request_id, step_order"),
                @Index(name = "idx_workflow_request_steps_escalation", columnList = "status, sla_due_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = "requestStepId")
public class WorkflowRequestStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "request_step_id", updatable = false, nullable = false)
    private UUID requestStepId;

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    /** 1, 2, 3 ... same number = parallel */
    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    /** snapshot from definition */
    @Column(name = "step_name", nullable = false)
    private String stepName;

    /** snapshot from definition */
    @Column(name = "approver_role", nullable = false)
    private String approverRole;

    /** snapshot from definition */
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_type", nullable = false, length = 20)
    private ApprovalType approvalType;

    @Column(name = "quorum_count")
    private Integer quorumCount;

    /** nullable, specific approver */
    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(name = "delegate_to")
    private UUID delegateTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RequestStepStatus status;

    /** nullable, Keycloak sub */
    @Column(name = "decided_by")
    private UUID decidedBy;

    /** display snapshot */
    @Column(name = "decider_name")
    private String deciderName;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "sla_due_at", nullable = false)
    private Instant slaDueAt;

    @Column(name = "escalated_at")
    private Instant escalatedAt;

    @Column(name = "decided_at")
    private Instant decidedAt;
}
