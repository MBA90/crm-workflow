package com.crm.workflow.mapper;

import com.crm.workflow.domain.WorkflowRequest;
import com.crm.workflow.domain.WorkflowRequestStep;
import com.crm.workflow.dto.WorkflowRequestDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = WorkflowRequestStepMapper.class)
public interface WorkflowRequestMapper {

    WorkflowRequestDto toDto(WorkflowRequest request, List<WorkflowRequestStep> steps);
}
