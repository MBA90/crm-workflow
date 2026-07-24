package com.crm.workflow.service.impl;

import com.crm.workflow.domain.ApprovalHistory;
import com.crm.workflow.repository.ApprovalHistoryRepository;
import com.crm.workflow.service.ApprovalHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ApprovalHistoryServiceImpl implements ApprovalHistoryService {

    private final ApprovalHistoryRepository historyRepository;

    @Override
    public ApprovalHistory save(ApprovalHistory history) {
        return historyRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalHistory> findByRequestIdOrderByOccurredAtAsc(UUID requestId) {
        return historyRepository.findByRequestIdOrderByOccurredAtAsc(requestId);
    }
}
