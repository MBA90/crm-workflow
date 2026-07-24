package com.crm.workflow.service.impl;

import com.crm.workflow.domain.WorkflowDefinitionStep;
import com.crm.workflow.repository.WorkflowDefinitionStepRepository;
import com.crm.workflow.service.WorkflowDefinitionStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowDefinitionStepServiceImpl implements WorkflowDefinitionStepService {

    private final WorkflowDefinitionStepRepository definitionStepRepository;

    @Override
    public List<WorkflowDefinitionStep> saveAll(List<WorkflowDefinitionStep> steps) {
        return definitionStepRepository.saveAll(steps);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowDefinitionStep> findByDefinitionIdOrderByStepOrderAsc(UUID definitionId) {
        return definitionStepRepository.findByDefinitionIdOrderByStepOrderAsc(definitionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowDefinitionStep> findByDefinitionIdInOrderByStepOrderAsc(Collection<UUID> definitionIds) {
        return definitionStepRepository.findByDefinitionIdInOrderByStepOrderAsc(definitionIds);
    }
}
