package com.crm.workflow.service;

import com.crm.workflow.domain.enums.OverallStatus;
import com.crm.workflow.dto.request.WorkflowRequestCreateRequest;
import com.crm.workflow.dto.request.WorkflowRequestStepDecisionRequest;
import com.crm.workflow.dto.WorkflowRequestDto;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface WorkflowRequestServiceFacade {

    WorkflowRequestDto create(WorkflowRequestCreateRequest createRequest);

    WorkflowRequestDto submit(UUID requestId);

    WorkflowRequestDto getById(UUID requestId);

    List<WorkflowRequestDto> list(Collection<OverallStatus> overallStatuses);

    WorkflowRequestDto decide(UUID requestStepId, WorkflowRequestStepDecisionRequest decision);
}
