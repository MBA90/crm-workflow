package com.crm.workflow.service.impl;

import com.crm.workflow.domain.WorkflowDefinition;
import com.crm.workflow.domain.enums.EntityType;
import com.crm.workflow.domain.enums.WorkflowAction;
import com.crm.workflow.repository.WorkflowDefinitionRepository;
import com.crm.workflow.service.WorkflowDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowDefinitionServiceImpl implements WorkflowDefinitionService {

    private final WorkflowDefinitionRepository definitionRepository;

    @Override
    public WorkflowDefinition save(WorkflowDefinition definition) {
        return definitionRepository.save(definition);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WorkflowDefinition> findById(UUID definitionId) {
        return definitionRepository.findById(definitionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WorkflowDefinition> findByEntityTypeAndActionAndVersion(
            EntityType entityType, WorkflowAction action, Integer version) {
        return definitionRepository.findByEntityTypeAndActionAndVersion(entityType, action, version);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WorkflowDefinition> findByEntityTypeAndActionAndActiveTrue(EntityType entityType, WorkflowAction action) {
        return definitionRepository.findByEntityTypeAndActionAndActiveTrue(entityType, action);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WorkflowDefinition> findTopByEntityTypeAndActionOrderByVersionDesc(EntityType entityType, WorkflowAction action) {
        return definitionRepository.findTopByEntityTypeAndActionOrderByVersionDesc(entityType, action);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowDefinition> findByEntityTypeAndActionOrderByVersionDesc(EntityType entityType, WorkflowAction action) {
        return definitionRepository.findByEntityTypeAndActionOrderByVersionDesc(entityType, action);
    }
}
