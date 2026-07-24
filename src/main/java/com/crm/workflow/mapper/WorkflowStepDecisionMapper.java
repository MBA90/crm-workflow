package com.crm.workflow.mapper;

import com.crm.workflow.domain.WorkflowStepDecision;
import com.crm.workflow.dto.WorkflowStepDecisionDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkflowStepDecisionMapper {

    WorkflowStepDecisionDto toDto(WorkflowStepDecision decision);
}
