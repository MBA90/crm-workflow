package com.crm.workflow.repository;

import com.crm.workflow.domain.WorkflowRequestStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowRequestStepRepository extends JpaRepository<WorkflowRequestStep, UUID> {

    List<WorkflowRequestStep> findByRequest_RequestIdOrderByStepOrderAsc(UUID requestId);

    List<WorkflowRequestStep> findByRequest_RequestIdAndStepOrder(UUID requestId, Integer stepOrder);
}
