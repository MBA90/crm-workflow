package com.crm.workflow.repository;

import com.crm.workflow.domain.WorkflowDefinitionStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowDefinitionStepRepository extends JpaRepository<WorkflowDefinitionStep, UUID> {

    List<WorkflowDefinitionStep> findByDefinition_DefinitionIdOrderByStepOrderAsc(UUID definitionId);
}
