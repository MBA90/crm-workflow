package com.crm.workflow.service.impl;

import com.crm.workflow.domain.WorkflowRequestStep;
import com.crm.workflow.repository.WorkflowRequestStepRepository;
import com.crm.workflow.service.WorkflowRequestStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowRequestStepServiceImpl implements WorkflowRequestStepService {

    private final WorkflowRequestStepRepository requestStepRepository;

    @Override
    public WorkflowRequestStep save(WorkflowRequestStep step) {
        return requestStepRepository.save(step);
    }

    @Override
    public List<WorkflowRequestStep> saveAll(List<WorkflowRequestStep> steps) {
        return requestStepRepository.saveAll(steps);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WorkflowRequestStep> findById(UUID requestStepId) {
        return requestStepRepository.findById(requestStepId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowRequestStep> findByRequestIdOrderByStepOrderAsc(UUID requestId) {
        return requestStepRepository.findByRequestIdOrderByStepOrderAsc(requestId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowRequestStep> findByRequestIdAndStepOrder(UUID requestId, Integer stepOrder) {
        return requestStepRepository.findByRequestIdAndStepOrder(requestId, stepOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowRequestStep> findByRequestIdInOrderByStepOrderAsc(Collection<UUID> requestIds) {
        return requestStepRepository.findByRequestIdInOrderByStepOrderAsc(requestIds);
    }
}
