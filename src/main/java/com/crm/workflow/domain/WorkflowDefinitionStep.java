package com.crm.workflow.domain;

import com.crm.workflow.domain.converter.JsonNodeConverter;
import com.crm.workflow.domain.enums.ApprovalType;
import com.crm.workflow.domain.enums.OnRejectAction;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnTransformer;

import java.util.UUID;

@Entity
@Table(
        name = "workflow_definition_steps",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_workflow_definition_steps_definition_order_name",
                columnNames = {"definition_id", "step_order", "step_name"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "definition")
@EqualsAndHashCode(of = "stepId")
public class WorkflowDefinitionStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "step_id", updatable = false, nullable = false)
    private UUID stepId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "definition_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_workflow_definition_steps_definition")
    )
    private WorkflowDefinition definition;

    /** 1, 2, 3 ... same number = parallel */
    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    /** Keycloak client role required */
    @Column(name = "approver_role", nullable = false)
    private String approverRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_type", nullable = false, length = 20)
    private ApprovalType approvalType;

    /** nullable, used when approvalType = QUORUM */
    @Column(name = "quorum_count")
    private Integer quorumCount;

    /** nullable, skip rule for this step */
    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "condition", columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private JsonNode condition;

    @Column(name = "sla_hours", nullable = false)
    private Integer slaHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "on_reject", nullable = false, length = 20)
    private OnRejectAction onReject;

    /** nullable, target step_order when onReject = RETURN_TO_STEP */
    @Column(name = "return_to_step")
    private Integer returnToStep;
}
