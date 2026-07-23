package com.crm.workflow.mapper;

import com.crm.workflow.domain.WorkflowRequest;
import com.crm.workflow.dto.WorkflowRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = WorkflowRequestStepMapper.class)
public interface WorkflowRequestMapper {

    @Mapping(target = "definitionId", source = "definition.definitionId")
    WorkflowRequestDto toDto(WorkflowRequest request);

    List<WorkflowRequestDto> toDtoList(List<WorkflowRequest> requests);
}
