package com.crm.workflow.service;

import com.crm.workflow.domain.WorkflowStepDecision;
import com.crm.workflow.domain.enums.DecisionOutcome;

import java.util.List;
import java.util.UUID;

public interface WorkflowStepDecisionService {

    WorkflowStepDecision save(WorkflowStepDecision decision);

    List<WorkflowStepDecision> findByRequestStepId(UUID requestStepId);

    boolean existsByRequestStepIdAndDecidedBy(UUID requestStepId, UUID decidedBy);

    long countByRequestStepIdAndDecision(UUID requestStepId, DecisionOutcome decision);
}
