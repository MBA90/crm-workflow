package com.crm.workflow.service;

import com.crm.workflow.domain.WorkflowDefinition;
import com.crm.workflow.domain.enums.ApprovalType;
import com.crm.workflow.domain.enums.EntityType;
import com.crm.workflow.domain.enums.OnRejectAction;
import com.crm.workflow.domain.enums.WorkflowAction;
import com.crm.workflow.dto.WorkflowDefinitionCreateRequest;
import com.crm.workflow.dto.WorkflowDefinitionDto;
import com.crm.workflow.dto.WorkflowDefinitionStepRequest;
import com.crm.workflow.exception.InvalidWorkflowDefinitionException;
import com.crm.workflow.exception.WorkflowDefinitionNotFoundException;
import com.crm.workflow.mapper.WorkflowDefinitionMapper;
import com.crm.workflow.mapper.WorkflowDefinitionStepMapper;
import com.crm.workflow.repository.WorkflowDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowDefinitionService {

    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowDefinitionMapper definitionMapper;
    private final WorkflowDefinitionStepMapper stepMapper;

    public WorkflowDefinitionDto create(WorkflowDefinitionCreateRequest request) {
        request.steps().forEach(this::validateStep);

        int nextVersion = definitionRepository
                .findTopByEntityTypeAndActionOrderByVersionDesc(request.entityType(), request.action())
                .map(existing -> existing.getVersion() + 1)
                .orElse(1);

        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setEntityType(request.entityType());
        definition.setAction(request.action());
        definition.setName(request.name());
        definition.setVersion(nextVersion);
        definition.setActive(false);

        request.steps().forEach(stepRequest -> definition.addStep(stepMapper.toEntity(stepRequest)));

        return definitionMapper.toDto(definitionRepository.save(definition));
    }

    @Transactional(readOnly = true)
    public WorkflowDefinitionDto getById(UUID definitionId) {
        return definitionMapper.toDto(findOrThrow(definitionId));
    }

    @Transactional(readOnly = true)
    public List<WorkflowDefinitionDto> list(EntityType entityType, WorkflowAction action) {
        return definitionMapper.toDtoList(
                definitionRepository.findByEntityTypeAndActionOrderByVersionDesc(entityType, action)
        );
    }

    @Transactional(readOnly = true)
    public WorkflowDefinitionDto getActive(EntityType entityType, WorkflowAction action) {
        return definitionRepository.findByEntityTypeAndActionAndActiveTrue(entityType, action)
                .map(definitionMapper::toDto)
                .orElseThrow(() -> new WorkflowDefinitionNotFoundException(entityType, action));
    }

    public WorkflowDefinitionDto activate(UUID definitionId) {
        WorkflowDefinition definition = findOrThrow(definitionId);

        definitionRepository.findByEntityTypeAndActionAndActiveTrue(definition.getEntityType(), definition.getAction())
                .filter(current -> !current.getDefinitionId().equals(definitionId))
                .ifPresent(current -> current.setActive(false));

        definition.setActive(true);
        return definitionMapper.toDto(definition);
    }

    private WorkflowDefinition findOrThrow(UUID definitionId) {
        return definitionRepository.findById(definitionId)
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
