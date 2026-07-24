package com.crm.workflow.service.impl;

import com.crm.workflow.domain.WorkflowDefinition;
import com.crm.workflow.domain.WorkflowDefinitionStep;
import com.crm.workflow.domain.enums.ApprovalType;
import com.crm.workflow.domain.enums.EntityType;
import com.crm.workflow.domain.enums.OnRejectAction;
import com.crm.workflow.domain.enums.WorkflowAction;
import com.crm.workflow.dto.request.WorkflowDefinitionCreateRequest;
import com.crm.workflow.dto.request.WorkflowDefinitionStepRequest;
import com.crm.workflow.dto.WorkflowDefinitionDto;
import com.crm.workflow.exception.InvalidWorkflowDefinitionException;
import com.crm.workflow.exception.WorkflowDefinitionNotFoundException;
import com.crm.workflow.mapper.WorkflowDefinitionMapper;
import com.crm.workflow.mapper.WorkflowDefinitionStepMapper;
import com.crm.workflow.service.WorkflowDefinitionService;
import com.crm.workflow.service.WorkflowDefinitionServiceFacade;
import com.crm.workflow.service.WorkflowDefinitionStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowDefinitionServiceFacadeImpl implements WorkflowDefinitionServiceFacade {

    private final WorkflowDefinitionService definitionService;
    private final WorkflowDefinitionStepService definitionStepService;
    private final WorkflowDefinitionMapper definitionMapper;
    private final WorkflowDefinitionStepMapper stepMapper;

    @Override
    public WorkflowDefinitionDto create(WorkflowDefinitionCreateRequest request) {
        request.steps().forEach(this::validateStep);

        int nextVersion = definitionService
                .findTopByEntityTypeAndActionOrderByVersionDesc(request.entityType(), request.action())
                .map(existing -> existing.getVersion() + 1)
                .orElse(1);

        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setEntityType(request.entityType());
        definition.setAction(request.action());
        definition.setName(request.name());
        definition.setVersion(nextVersion);
        definition.setActive(false);
        WorkflowDefinition savedDefinition = definitionService.save(definition);

        List<WorkflowDefinitionStep> steps = request.steps().stream()
                .map(stepRequest -> {
                    WorkflowDefinitionStep step = stepMapper.toEntity(stepRequest);
                    step.setDefinitionId(savedDefinition.getDefinitionId());
                    return step;
                })
                .toList();
        definitionStepService.saveAll(steps);

        return definitionMapper.toDto(savedDefinition, steps);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowDefinitionDto getById(UUID definitionId) {
        WorkflowDefinition definition = findOrThrow(definitionId);
        List<WorkflowDefinitionStep> steps = definitionStepService.findByDefinitionIdOrderByStepOrderAsc(definitionId);
        return definitionMapper.toDto(definition, steps);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowDefinitionDto> list(EntityType entityType, WorkflowAction action) {
        List<WorkflowDefinition> definitions = definitionService.findByEntityTypeAndActionOrderByVersionDesc(entityType, action);
        List<UUID> definitionIds = definitions.stream().map(WorkflowDefinition::getDefinitionId).toList();
        Map<UUID, List<WorkflowDefinitionStep>> stepsByDefinitionId = definitionStepService
                .findByDefinitionIdInOrderByStepOrderAsc(definitionIds).stream()
                .collect(Collectors.groupingBy(WorkflowDefinitionStep::getDefinitionId));

        return definitions.stream()
                .map(definition -> definitionMapper.toDto(definition, stepsByDefinitionId.getOrDefault(definition.getDefinitionId(), List.of())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowDefinitionDto getActive(EntityType entityType, WorkflowAction action) {
        WorkflowDefinition definition = definitionService.findByEntityTypeAndActionAndActiveTrue(entityType, action)
                .orElseThrow(() -> new WorkflowDefinitionNotFoundException(entityType, action));
        List<WorkflowDefinitionStep> steps = definitionStepService.findByDefinitionIdOrderByStepOrderAsc(definition.getDefinitionId());
        return definitionMapper.toDto(definition, steps);
    }

    @Override
    public WorkflowDefinitionDto activate(UUID definitionId) {
        WorkflowDefinition definition = findOrThrow(definitionId);

        definitionService.findByEntityTypeAndActionAndActiveTrue(definition.getEntityType(), definition.getAction())
                .filter(current -> !current.getDefinitionId().equals(definitionId))
                .ifPresent(current -> current.setActive(false));

        definition.setActive(true);
        List<WorkflowDefinitionStep> steps = definitionStepService.findByDefinitionIdOrderByStepOrderAsc(definitionId);
        return definitionMapper.toDto(definition, steps);
    }

    private WorkflowDefinition findOrThrow(UUID definitionId) {
        return definitionService.findById(definitionId)
                .orElseThrow(() -> new WorkflowDefinitionNotFoundException(definitionId));
    }

    private void validateStep(WorkflowDefinitionStepRequest step) {
        if (step.approvalType() == ApprovalType.QUORUM && step.quorumCount() == null) {
            throw new InvalidWorkflowDefinitionException(
                    "quorumCount is required when approvalType is QUORUM (step '" + step.stepName() + "')");
        }
        if (step.onReject() == OnRejectAction.RETURN_TO_STEP && step.returnToStep() == null) {
            throw new InvalidWorkflowDefinitionException(
                    "returnToStep is required when onReject is RETURN_TO_STEP (step '" + step.stepName() + "')");
        }
    }
}
