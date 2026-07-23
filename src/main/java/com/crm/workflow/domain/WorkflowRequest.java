package com.crm.workflow.domain;

import com.crm.workflow.domain.converter.JsonNodeConverter;
import com.crm.workflow.domain.enums.OverallStatus;
import com.crm.workflow.domain.enums.WorkflowAction;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnTransformer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "workflow_requests",
        indexes = {
                @Index(name = "idx_workflow_requests_status_step", columnList = "overall_status, current_step"),
                @Index(name = "idx_workflow_requests_entity", columnList = "entity_type, entity_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "steps")
@EqualsAndHashCode(of = "requestId")
public class WorkflowRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "request_id", updatable = false, nullable = false)
    private UUID requestId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "definition_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_workflow_requests_definition")
    )
    private WorkflowDefinition definition;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    /** NULL for create actions */
    @Column(name = "entity_id")
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private WorkflowAction action;

    /** opaque to this service */
    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private JsonNode payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_status", nullable = false, length = 20)
    private OverallStatus overallStatus;

    /** pointer into request_steps */
    @Column(name = "current_step")
    private Integer currentStep;

    /** Keycloak sub */
    @Column(name = "requested_by", nullable = false)
    private UUID requestedBy;

    /** display snapshot */
    @Column(name = "requester_name", nullable = false)
    private String requesterName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    private List<WorkflowRequestStep> steps = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public void addStep(WorkflowRequestStep step) {
        steps.add(step);
        step.setRequest(this);
    }

    public void removeStep(WorkflowRequestStep step) {
        steps.remove(step);
        step.setRequest(null);
    }
}
