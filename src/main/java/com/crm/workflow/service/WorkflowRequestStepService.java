package com.crm.workflow.service;

import com.crm.workflow.domain.WorkflowRequestStep;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowRequestStepService {

    WorkflowRequestStep save(WorkflowRequestStep step);

    List<WorkflowRequestStep> saveAll(List<WorkflowRequestStep> steps);

    Optional<WorkflowRequestStep> findById(UUID requestStepId);

    List<WorkflowRequestStep> findByRequestIdOrderByStepOrderAsc(UUID requestId);

    List<WorkflowRequestStep> findByRequestIdAndStepOrder(UUID requestId, Integer stepOrder);

    List<WorkflowRequestStep> findByRequestIdInOrderByStepOrderAsc(Collection<UUID> requestIds);
}
