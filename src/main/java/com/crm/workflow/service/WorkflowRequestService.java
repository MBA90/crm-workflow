package com.crm.workflow.service;

import com.crm.workflow.domain.WorkflowRequest;
import com.crm.workflow.domain.enums.OverallStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowRequestService {

    WorkflowRequest save(WorkflowRequest request);

    Optional<WorkflowRequest> findById(UUID requestId);

    List<WorkflowRequest> findByOverallStatusIn(Collection<OverallStatus> overallStatuses);

    boolean existsByEntityTypeAndEntityIdAndOverallStatusIn(
            String entityType, UUID entityId, Collection<OverallStatus> overallStatuses);
}
