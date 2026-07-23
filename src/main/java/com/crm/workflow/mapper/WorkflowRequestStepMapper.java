package com.crm.workflow.mapper;

import com.crm.workflow.domain.WorkflowDefinitionStep;
import com.crm.workflow.domain.WorkflowRequestStep;
import com.crm.workflow.dto.WorkflowRequestStepDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkflowRequestStepMapper {

    WorkflowRequestStepDto toDto(WorkflowRequestStep step);

    @Mapping(target = "requestStepId", ignore = true)
    @Mapping(target = "requestId", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "delegateTo", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "decidedBy", ignore = true)
    @Mapping(target = "deciderName", ignore = true)
    @Mapping(target = "comment", ignore = true)
    @Mapping(target = "slaDueAt", ignore = true)
    @Mapping(target = "escalatedAt", ignore = true)
    @Mapping(target = "decidedAt", ignore = true)
    WorkflowRequestStep toEntity(WorkflowDefinitionStep definitionStep);
}
