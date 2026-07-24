package com.crm.workflow.service.impl;

import com.crm.workflow.domain.WorkflowStepDecision;
import com.crm.workflow.domain.enums.DecisionOutcome;
import com.crm.workflow.repository.WorkflowStepDecisionRepository;
import com.crm.workflow.service.WorkflowStepDecisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowStepDecisionServiceImpl implements WorkflowStepDecisionService {

    private final WorkflowStepDecisionRepository stepDecisionRepository;

    @Override
    public WorkflowStepDecision save(WorkflowStepDecision decision) {
        return stepDecisionRepository.save(decision);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowStepDecision> findByRequestStepId(UUID requestStepId) {
        return stepDecisionRepository.findByRequestStepId(requestStepId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByRequestStepIdAndDecidedBy(UUID requestStepId, UUID decidedBy) {
        return stepDecisionRepository.existsByRequestStepIdAndDecidedBy(requestStepId, decidedBy);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRequestStepIdAndDecision(UUID requestStepId, DecisionOutcome decision) {
        return stepDecisionRepository.countByRequestStepIdAndDecision(requestStepId, decision);
    }
}
