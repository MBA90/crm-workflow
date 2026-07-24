package com.crm.workflow.service;

import com.crm.workflow.domain.WorkflowDefinitionStep;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface WorkflowDefinitionStepService {

    List<WorkflowDefinitionStep> saveAll(List<WorkflowDefinitionStep> steps);

    List<WorkflowDefinitionStep> findByDefinitionIdOrderByStepOrderAsc(UUID definitionId);

    List<WorkflowDefinitionStep> findByDefinitionIdInOrderByStepOrderAsc(Collection<UUID> definitionIds);
}
