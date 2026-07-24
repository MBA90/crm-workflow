package com.crm.workflow.web.rest;

import com.crm.workflow.domain.enums.OverallStatus;
import com.crm.workflow.dto.request.WorkflowRequestCreateRequest;
import com.crm.workflow.dto.request.WorkflowRequestStepDecisionRequest;
import com.crm.workflow.dto.WorkflowRequestDto;
import com.crm.workflow.service.WorkflowRequestServiceFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflow-requests")
@RequiredArgsConstructor
public class WorkflowRequestController {

    private final WorkflowRequestServiceFacade workflowRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkflowRequestDto create(@Valid @RequestBody WorkflowRequestCreateRequest request) {
        return workflowRequestService.create(request);
    }

    @PostMapping("/{requestId}/submit")
    public WorkflowRequestDto submit(@PathVariable UUID requestId) {
        return workflowRequestService.submit(requestId);
    }

    @GetMapping("/{requestId}")
    public WorkflowRequestDto getById(@PathVariable UUID requestId) {
        return workflowRequestService.getById(requestId);
    }

    @GetMapping
    public List<WorkflowRequestDto> list(@RequestParam List<OverallStatus> statuses) {
        return workflowRequestService.list(statuses);
    }

    @PostMapping("/steps/{requestStepId}/decide")
    public WorkflowRequestDto decide(
            @PathVariable UUID requestStepId,
            @Valid @RequestBody WorkflowRequestStepDecisionRequest decision) {
        return workflowRequestService.decide(requestStepId, decision);
    }
}
