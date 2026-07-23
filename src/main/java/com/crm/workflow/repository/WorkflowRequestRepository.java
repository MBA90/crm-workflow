package com.crm.workflow.repository;

import com.crm.workflow.domain.WorkflowRequest;
import com.crm.workflow.domain.enums.OverallStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface WorkflowRequestRepository extends JpaRepository<WorkflowRequest, UUID> {

    List<WorkflowRequest> findByOverallStatusIn(Collection<OverallStatus> overallStatuses);

    boolean existsByEntityTypeAndEntityIdAndOverallStatusIn(
            String entityType, UUID entityId, Collection<OverallStatus> overallStatuses);
}
