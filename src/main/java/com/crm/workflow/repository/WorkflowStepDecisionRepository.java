package com.crm.workflow.repository;

import com.crm.workflow.domain.WorkflowStepDecision;
import com.crm.workflow.domain.enums.DecisionOutcome;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowStepDecisionRepository extends JpaRepository<WorkflowStepDecision, UUID> {

    List<WorkflowStepDecision> findByRequestStepId(UUID requestStepId);

    boolean existsByRequestStepIdAndDecidedBy(UUID requestStepId, UUID decidedBy);

    long countByRequestStepIdAndDecision(UUID requestStepId, DecisionOutcome decision);
}
