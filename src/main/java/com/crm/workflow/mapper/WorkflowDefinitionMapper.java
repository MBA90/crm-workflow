package com.crm.workflow.mapper;

import com.crm.workflow.domain.WorkflowDefinition;
import com.crm.workflow.dto.WorkflowDefinitionDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = WorkflowDefinitionStepMapper.class)
public interface WorkflowDefinitionMapper {

    WorkflowDefinitionDto toDto(WorkflowDefinition definition);

    List<WorkflowDefinitionDto> toDtoList(List<WorkflowDefinition> definitions);
}
