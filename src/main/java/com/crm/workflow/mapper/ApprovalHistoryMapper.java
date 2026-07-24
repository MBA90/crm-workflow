package com.crm.workflow.mapper;

import com.crm.workflow.domain.ApprovalHistory;
import com.crm.workflow.dto.ApprovalHistoryDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApprovalHistoryMapper {

    ApprovalHistoryDto toDto(ApprovalHistory history);
}
