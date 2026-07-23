package com.crm.workflow.repository;

import com.crm.workflow.domain.WorkflowRequestStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface WorkflowRequestStepRepository extends JpaRepository<WorkflowRequestStep, UUID> {

    List<WorkflowRequestStep> findByRequestIdOrderByStepOrderAsc(UUID requestId);

    List<WorkflowRequestStep> findByRequestIdAndStepOrder(UUID requestId, Integer stepOrder);

    List<WorkflowRequestStep> findByRequestIdInOrderByStepOrderAsc(Collection<UUID> requestIds);
}
