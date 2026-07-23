package com.crm.workflow.service;

import com.crm.workflow.domain.WorkflowDefinition;
import com.crm.workflow.domain.WorkflowDefinitionStep;
import com.crm.workflow.domain.WorkflowRequest;
import com.crm.workflow.domain.WorkflowRequestStep;
import com.crm.workflow.domain.enums.ApprovalType;
import com.crm.workflow.domain.enums.OverallStatus;
import com.crm.workflow.domain.enums.RequestStepStatus;
import com.crm.workflow.dto.request.WorkflowRequestCreateRequest;
import com.crm.workflow.dto.request.WorkflowRequestStepDecisionRequest;
import com.crm.workflow.dto.WorkflowRequestDto;
import com.crm.workflow.exception.InvalidWorkflowRequestException;
import com.crm.workflow.exception.WorkflowDefinitionNotFoundException;
import com.crm.workflow.exception.WorkflowRequestNotFoundException;
import com.crm.workflow.exception.WorkflowRequestStepNotFoundException;
import com.crm.workflow.mapper.WorkflowRequestMapper;
import com.crm.workflow.mapper.WorkflowRequestStepMapper;
import com.crm.workflow.repository.WorkflowDefinitionRepository;
import com.crm.workflow.repository.WorkflowRequestRepository;
import com.crm.workflow.repository.WorkflowRequestStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowRequestService {

    private static final List<OverallStatus> IN_FLIGHT_STATUSES = List.of(OverallStatus.IN_PROGRESS);

    private final WorkflowRequestRepository requestRepository;
    private final WorkflowRequestStepRepository requestStepRepository;
    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowRequestMapper requestMapper;
    private final WorkflowRequestStepMapper requestStepMapper;

    public WorkflowRequestDto create(WorkflowRequestCreateRequest createRequest) {
        WorkflowDefinition definition = definitionRepository.findById(createRequest.definitionId())
                .orElseThrow(() -> new WorkflowDefinitionNotFoundException(createRequest.definitionId()));

        if (definition.getAction() != createRequest.action()) {
            throw new InvalidWorkflowRequestException(
                    "Definition action (" + definition.getAction() + ") does not match request action (" + createRequest.action() + ")");
        }

        WorkflowRequest request = new WorkflowRequest();
        request.setDefinition(definition);
        request.setEntityType(createRequest.entityType());
        request.setEntityId(createRequest.entityId());
        request.setAction(createRequest.action());
        request.setPayload(createRequest.payload());
        request.setOverallStatus(OverallStatus.DRAFT);
        request.setRequestedBy(createRequest.requestedBy());
        request.setRequesterName(createRequest.requesterName());

        return requestMapper.toDto(requestRepository.save(request));
    }

    public WorkflowRequestDto submit(UUID requestId) {
        WorkflowRequest request = findOrThrow(requestId);

        if (request.getOverallStatus() != OverallStatus.DRAFT) {
            throw new InvalidWorkflowRequestException("Only draft requests can be submitted: " + requestId);
        }

        if (request.getEntityId() != null
                && requestRepository.existsByEntityTypeAndEntityIdAndOverallStatusIn(
                        request.getEntityType(), request.getEntityId(), IN_FLIGHT_STATUSES)) {
            throw new InvalidWorkflowRequestException(
                    "A request is already in progress for " + request.getEntityType() + "/" + request.getEntityId());
        }

        List<WorkflowDefinitionStep> definitionSteps = request.getDefinition().getSteps();
        if (definitionSteps.isEmpty()) {
            throw new InvalidWorkflowRequestException("Definition has no steps: " + request.getDefinition().getDefinitionId());
        }

        Instant now = Instant.now();
        int firstStepOrder = definitionSteps.get(0).getStepOrder();

        for (WorkflowDefinitionStep definitionStep : definitionSteps) {
            WorkflowRequestStep step = requestStepMapper.fromDefinitionStep(definitionStep);
            step.setSlaDueAt(now.plus(Duration.ofHours(definitionStep.getSlaHours())));
            step.setStatus(definitionStep.getStepOrder() == firstStepOrder
                    ? RequestStepStatus.ACTIVE
                    : RequestStepStatus.PENDING);
            request.addStep(step);
        }

        request.setOverallStatus(OverallStatus.IN_PROGRESS);
        request.setCurrentStep(firstStepOrder);

        return requestMapper.toDto(request);
    }

    @Transactional(readOnly = true)
    public WorkflowRequestDto getById(UUID requestId) {
        return requestMapper.toDto(findOrThrow(requestId));
    }

    @Transactional(readOnly = true)
    public List<WorkflowRequestDto> list(Collection<OverallStatus> overallStatuses) {
        return requestMapper.toDtoList(requestRepository.findByOverallStatusIn(overallStatuses));
    }

    public WorkflowRequestDto decide(UUID requestStepId, WorkflowRequestStepDecisionRequest decision) {
        WorkflowRequestStep step = requestStepRepository.findById(requestStepId)
                .orElseThrow(() -> new WorkflowRequestStepNotFoundException(requestStepId));

        if (step.getStatus() != RequestStepStatus.ACTIVE) {
            throw new InvalidWorkflowRequestException("Step is not active: " + requestStepId);
        }

        Instant now = Instant.now();
        step.setDecidedBy(decision.decidedBy());
        step.setDeciderName(decision.deciderName());
        step.setComment(decision.comment());
        step.setDecidedAt(now);

        WorkflowRequest request = step.getRequest();

        if (!decision.approved()) {
            step.setStatus(RequestStepStatus.REJECTED);
            request.setOverallStatus(OverallStatus.REJECTED);
            request.setCompletedAt(now);
            return requestMapper.toDto(request);
        }

        step.setStatus(RequestStepStatus.APPROVED);

        List<WorkflowRequestStep> siblings = requestStepRepository
                .findByRequest_RequestIdAndStepOrder(request.getRequestId(), step.getStepOrder());

        if (!isGroupSatisfied(step.getApprovalType(), siblings)) {
            return requestMapper.toDto(request);
        }

        Integer nextStepOrder = request.getSteps().stream()
                .map(WorkflowRequestStep::getStepOrder)
                .filter(order -> order > step.getStepOrder())
                .min(Integer::compareTo)
                .orElse(null);

        if (nextStepOrder == null) {
            request.setOverallStatus(OverallStatus.APPROVED);
            request.setCompletedAt(now);
        } else {
            request.setCurrentStep(nextStepOrder);
            request.getSteps().stream()
                    .filter(s -> s.getStepOrder().equals(nextStepOrder))
                    .forEach(s -> s.setStatus(RequestStepStatus.ACTIVE));
        }

        return requestMapper.toDto(request);
    }

    private boolean isGroupSatisfied(ApprovalType approvalType, List<WorkflowRequestStep> siblings) {
        return switch (approvalType) {
            case SINGLE -> true;
            case ALL -> siblings.stream().allMatch(s -> s.getStatus() == RequestStepStatus.APPROVED);
            case QUORUM -> {
                long approvedCount = siblings.stream().filter(s -> s.getStatus() == RequestStepStatus.APPROVED).count();
                Integer quorumCount = siblings.get(0).getQuorumCount();
                yield quorumCount != null && approvedCount >= quorumCount;
            }
        };
    }

    private WorkflowRequest findOrThrow(UUID requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new WorkflowRequestNotFoundException(requestId));
    }
}
