package com.crm.workflow.mapper;

import com.crm.workflow.domain.WorkflowDefinitionStep;
import com.crm.workflow.dto.request.WorkflowDefinitionStepRequest;
import com.crm.workflow.dto.WorkflowDefinitionStepDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkflowDefinitionStepMapper {

    WorkflowDefinitionStepDto toDto(WorkflowDefinitionStep step);

    List<WorkflowDefinitionStepDto> toDtoList(List<WorkflowDefinitionStep> steps);

    @Mapping(target = "stepId", ignore = true)
    @Mapping(target = "definitionId", ignore = true)
    WorkflowDefinitionStep toEntity(WorkflowDefinitionStepRequest request);
}
