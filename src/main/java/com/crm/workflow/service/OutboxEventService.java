package com.crm.workflow.service;

import com.crm.workflow.domain.OutboxEvent;

import java.util.List;

public interface OutboxEventService {

    OutboxEvent save(OutboxEvent event);

    List<OutboxEvent> findByPublishedFalseOrderByCreatedAtAsc();
}
