package com.crm.workflow.service;

import com.crm.workflow.domain.ApprovalHistory;

import java.util.List;
import java.util.UUID;

public interface ApprovalHistoryService {

    ApprovalHistory save(ApprovalHistory history);

    List<ApprovalHistory> findByRequestIdOrderByOccurredAtAsc(UUID requestId);
}
