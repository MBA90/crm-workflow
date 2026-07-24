package com.crm.workflow.service.impl;

import com.crm.workflow.domain.OutboxEvent;
import com.crm.workflow.repository.OutboxEventRepository;
import com.crm.workflow.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OutboxEventServiceImpl implements OutboxEventService {

    private final OutboxEventRepository outboxRepository;

    @Override
    public OutboxEvent save(OutboxEvent event) {
        return outboxRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxEvent> findByPublishedFalseOrderByCreatedAtAsc() {
        return outboxRepository.findByPublishedFalseOrderByCreatedAtAsc();
    }
}
