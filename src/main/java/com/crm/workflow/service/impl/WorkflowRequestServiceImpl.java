package com.crm.workflow.service.impl;

import com.crm.workflow.domain.WorkflowRequest;
import com.crm.workflow.domain.enums.OverallStatus;
import com.crm.workflow.repository.WorkflowRequestRepository;
import com.crm.workflow.service.WorkflowRequestService;
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
public class WorkflowRequestServiceImpl implements WorkflowRequestService {

    private final WorkflowRequestRepository requestRepository;

    @Override
    public WorkflowRequest save(WorkflowRequest request) {
        return requestRepository.save(request);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WorkflowRequest> findById(UUID requestId) {
        return requestRepository.findById(requestId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowRequest> findByOverallStatusIn(Collection<OverallStatus> overallStatuses) {
        return requestRepository.findByOverallStatusIn(overallStatuses);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEntityTypeAndEntityIdAndOverallStatusIn(
            String entityType, UUID entityId, Collection<OverallStatus> overallStatuses) {
        return requestRepository.existsByEntityTypeAndEntityIdAndOverallStatusIn(entityType, entityId, overallStatuses);
    }
}
