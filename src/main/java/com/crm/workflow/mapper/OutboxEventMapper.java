package com.crm.workflow.mapper;

import com.crm.workflow.domain.OutboxEvent;
import com.crm.workflow.dto.OutboxEventDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OutboxEventMapper {

    OutboxEventDto toDto(OutboxEvent event);
}
