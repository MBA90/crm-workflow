package com.crm.workflow.service;

import com.crm.workflow.domain.ApprovalHistory;
import com.crm.workflow.domain.OutboxEvent;
import com.crm.workflow.domain.WorkflowDefinition;
import com.crm.workflow.domain.WorkflowDefinitionStep;
import com.crm.workflow.domain.WorkflowRequest;
import com.crm.workflow.domain.WorkflowRequestStep;
import com.crm.workflow.domain.WorkflowStepDecision;
import com.crm.workflow.domain.enums.ApprovalType;
import com.crm.workflow.domain.enums.DecisionOutcome;
import com.crm.workflow.domain.enums.OutboxEventType;
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
import com.crm.workflow.repository.ApprovalHistoryRepository;
import com.crm.workflow.repository.OutboxEventRepository;
import com.crm.workflow.repository.WorkflowDefinitionRepository;
import com.crm.workflow.repository.WorkflowDefinitionStepRepository;
import com.crm.workflow.repository.WorkflowRequestRepository;
import com.crm.workflow.repository.WorkflowRequestStepRepository;
import com.crm.workflow.repository.WorkflowStepDecisionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowRequestService {

    private static final List<OverallStatus> IN_FLIGHT_STATUSES = List.of(OverallStatus.IN_PROGRESS);

    private final WorkflowRequestRepository requestRepository;
    private final WorkflowRequestStepRepository requestStepRepository;
    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowDefinitionStepRepository definitionStepRepository;
    private final WorkflowStepDecisionRepository stepDecisionRepository;
    private final ApprovalHistoryRepository historyRepository;
    private final OutboxEventRepository outboxRepository;
    private final WorkflowRequestMapper requestMapper;
    private final WorkflowRequestStepMapper requestStepMapper;
    private final ObjectMapper objectMapper;

    public WorkflowRequestDto create(WorkflowRequestCreateRequest createRequest) {
        WorkflowDefinition definition = definitionRepository.findById(createRequest.definitionId())
                .orElseThrow(() -> new WorkflowDefinitionNotFoundException(createRequest.definitionId()));

        if (definition.getAction() != createRequest.action()) {
            throw new InvalidWorkflowRequestException(
                    "Definition action (" + definition.getAction() + ") does not match request action (" + createRequest.action() + ")");
        }

        WorkflowRequest request = new WorkflowRequest();
        request.setDefinitionId(definition.getDefinitionId());
        request.setEntityType(createRequest.entityType());
        request.setEntityId(createRequest.entityId());
        request.setAction(createRequest.action());
        request.setPayload(createRequest.payload());
        request.setOverallStatus(OverallStatus.DRAFT);
        request.setRequestedBy(createRequest.requestedBy());
        request.setRequesterName(createRequest.requesterName());

        request = requestRepository.save(request);
        recordHistory(request.getRequestId(), null, request.getRequestedBy(), request.getRequesterName(),
                null, OverallStatus.DRAFT.name(), null, request.getCreatedAt());

        return requestMapper.toDto(request, List.of());
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

        WorkflowDefinition definition = definitionRepository.findById(request.getDefinitionId())
                .orElseThrow(() -> new WorkflowDefinitionNotFoundException(request.getDefinitionId()));

        List<WorkflowDefinitionStep> definitionSteps = definitionStepRepository
                .findByDefinitionIdOrderByStepOrderAsc(definition.getDefinitionId());
        if (definitionSteps.isEmpty()) {
            throw new InvalidWorkflowRequestException("Definition has no steps: " + definition.getDefinitionId());
        }

        Instant now = Instant.now();
        int firstStepOrder = definitionSteps.get(0).getStepOrder();

        List<WorkflowRequestStep> steps = new ArrayList<>();
        for (WorkflowDefinitionStep definitionStep : definitionSteps) {
            WorkflowRequestStep step = requestStepMapper.toEntity(definitionStep);
            step.setRequestId(request.getRequestId());
            step.setSlaDueAt(now.plus(Duration.ofHours(definitionStep.getSlaHours())));
            step.setStatus(definitionStep.getStepOrder() == firstStepOrder
                    ? RequestStepStatus.ACTIVE
                    : RequestStepStatus.PENDING);
            steps.add(step);
        }
        requestStepRepository.saveAll(steps);

        request.setOverallStatus(OverallStatus.IN_PROGRESS);
        request.setCurrentStep(firstStepOrder);

        recordHistory(request.getRequestId(), null, request.getRequestedBy(), request.getRequesterName(),
                OverallStatus.DRAFT.name(), OverallStatus.IN_PROGRESS.name(), null, now);

        return requestMapper.toDto(request, steps);
    }

    @Transactional(readOnly = true)
    public WorkflowRequestDto getById(UUID requestId) {
        WorkflowRequest request = findOrThrow(requestId);
        List<WorkflowRequestStep> steps = requestStepRepository.findByRequestIdOrderByStepOrderAsc(requestId);
        return requestMapper.toDto(request, steps);
    }

    @Transactional(readOnly = true)
    public List<WorkflowRequestDto> list(Collection<OverallStatus> overallStatuses) {
        List<WorkflowRequest> requests = requestRepository.findByOverallStatusIn(overallStatuses);
        List<UUID> requestIds = requests.stream().map(WorkflowRequest::getRequestId).toList();
        Map<UUID, List<WorkflowRequestStep>> stepsByRequestId = requestStepRepository
                .findByRequestIdInOrderByStepOrderAsc(requestIds).stream()
                .collect(Collectors.groupingBy(WorkflowRequestStep::getRequestId));

        return requests.stream()
                .map(request -> requestMapper.toDto(request, stepsByRequestId.getOrDefault(request.getRequestId(), List.of())))
                .toList();
    }

    public WorkflowRequestDto decide(UUID requestStepId, WorkflowRequestStepDecisionRequest decision) {
        WorkflowRequestStep step = requestStepRepository.findById(requestStepId)
                .orElseThrow(() -> new WorkflowRequestStepNotFoundException(requestStepId));

        if (step.getStatus() != RequestStepStatus.ACTIVE) {
            throw new InvalidWorkflowRequestException("Step is not active: " + requestStepId);
        }

        if (stepDecisionRepository.existsByRequestStepIdAndDecidedBy(requestStepId, decision.decidedBy())) {
            throw new InvalidWorkflowRequestException(
                    "Approver has already decided on this step: " + requestStepId);
        }

        Instant now = Instant.now();

        WorkflowStepDecision vote = new WorkflowStepDecision();
        vote.setRequestStepId(requestStepId);
        vote.setDecidedBy(decision.decidedBy());
        vote.setDeciderName(decision.deciderName());
        vote.setDecision(decision.approved() ? DecisionOutcome.APPROVED : DecisionOutcome.REJECTED);
        vote.setComment(decision.comment());
        vote.setDecidedAt(now);
        stepDecisionRepository.save(vote);

        step.setDecidedBy(decision.decidedBy());
        step.setDeciderName(decision.deciderName());
        step.setComment(decision.comment());
        step.setDecidedAt(now);

        WorkflowRequest request = findOrThrow(step.getRequestId());

        if (!decision.approved()) {
            step.setStatus(RequestStepStatus.REJECTED);
            request.setOverallStatus(OverallStatus.REJECTED);
            request.setCompletedAt(now);

            recordHistory(request.getRequestId(), step.getRequestStepId(), decision.decidedBy(), decision.deciderName(),
                    RequestStepStatus.ACTIVE.name(), RequestStepStatus.REJECTED.name(), decision.comment(), now);
            recordHistory(request.getRequestId(), null, decision.decidedBy(), decision.deciderName(),
                    OverallStatus.IN_PROGRESS.name(), OverallStatus.REJECTED.name(), null, now);
            publishOutboxEvent(request, OutboxEventType.WORKFLOW_REJECTED, now);

            return requestMapper.toDto(request, requestStepRepository.findByRequestIdOrderByStepOrderAsc(request.getRequestId()));
        }

        if (step.getApprovalType() == ApprovalType.QUORUM) {
            long approvedVotes = stepDecisionRepository.countByRequestStepIdAndDecision(requestStepId, DecisionOutcome.APPROVED);
            Integer quorumCount = step.getQuorumCount();
            if (quorumCount == null || approvedVotes < quorumCount) {
                return requestMapper.toDto(request, requestStepRepository.findByRequestIdOrderByStepOrderAsc(request.getRequestId()));
            }
        }

        step.setStatus(RequestStepStatus.APPROVED);
        recordHistory(request.getRequestId(), step.getRequestStepId(), decision.decidedBy(), decision.deciderName(),
                RequestStepStatus.ACTIVE.name(), RequestStepStatus.APPROVED.name(), decision.comment(), now);

        List<WorkflowRequestStep> siblings = requestStepRepository
                .findByRequestIdAndStepOrder(request.getRequestId(), step.getStepOrder());

        if (!isGroupSatisfied(step.getApprovalType(), siblings)) {
            return requestMapper.toDto(request, requestStepRepository.findByRequestIdOrderByStepOrderAsc(request.getRequestId()));
        }

        List<WorkflowRequestStep> allSteps = requestStepRepository.findByRequestIdOrderByStepOrderAsc(request.getRequestId());

        Integer nextStepOrder = allSteps.stream()
                .map(WorkflowRequestStep::getStepOrder)
                .filter(order -> order > step.getStepOrder())
                .min(Integer::compareTo)
                .orElse(null);

        if (nextStepOrder == null) {
            request.setOverallStatus(OverallStatus.APPROVED);
            request.setCompletedAt(now);
            recordHistory(request.getRequestId(), null, decision.decidedBy(), decision.deciderName(),
                    OverallStatus.IN_PROGRESS.name(), OverallStatus.APPROVED.name(), null, now);
            publishOutboxEvent(request, OutboxEventType.WORKFLOW_APPROVED, now);
        } else {
            request.setCurrentStep(nextStepOrder);
            allSteps.stream()
                    .filter(s -> s.getStepOrder().equals(nextStepOrder))
                    .forEach(s -> {
                        s.setStatus(RequestStepStatus.ACTIVE);
                        recordHistory(request.getRequestId(), s.getRequestStepId(), decision.decidedBy(), decision.deciderName(),
                                RequestStepStatus.PENDING.name(), RequestStepStatus.ACTIVE.name(), null, now);
                    });
        }

        return requestMapper.toDto(request, allSteps);
    }

    private void recordHistory(UUID requestId, UUID requestStepId, UUID actorId, String actorName,
            String fromStatus, String toStatus, String comment, Instant occurredAt) {
        ApprovalHistory history = new ApprovalHistory();
        history.setRequestId(requestId);
        history.setRequestStepId(requestStepId);
        history.setActorId(actorId);
        history.setActorName(actorName);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setComment(comment);
        history.setOccurredAt(occurredAt);
        historyRepository.save(history);
    }

    private void publishOutboxEvent(WorkflowRequest request, OutboxEventType eventType, Instant occurredAt) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("requestId", request.getRequestId().toString());
        payload.put("entityType", request.getEntityType());
        if (request.getEntityId() != null) {
            payload.put("entityId", request.getEntityId().toString());
        }
        payload.put("action", request.getAction().name());
        payload.put("occurredAt", occurredAt.toString());

        OutboxEvent event = new OutboxEvent();
        event.setAggregateId(request.getRequestId());
        event.setEventType(eventType);
        event.setPayload(payload);
        outboxRepository.save(event);
    }

    private boolean isGroupSatisfied(ApprovalType approvalType, List<WorkflowRequestStep> siblings) {
        return switch (approvalType) {
            // quorum is verified against workflow_step_decisions before the step is marked APPROVED
            case SINGLE, QUORUM -> true;
            case ALL -> siblings.stream().allMatch(s -> s.getStatus() == RequestStepStatus.APPROVED);
        };
    }

    private WorkflowRequest findOrThrow(UUID requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new WorkflowRequestNotFoundException(requestId));
    }
}
