package com.crm.workflow.web.rest;

import com.crm.workflow.domain.enums.EntityType;
import com.crm.workflow.domain.enums.WorkflowAction;
import com.crm.workflow.dto.WorkflowDefinitionCreateRequest;
import com.crm.workflow.dto.WorkflowDefinitionDto;
import com.crm.workflow.service.WorkflowDefinitionService;
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
@RequestMapping("/api/workflow-definitions")
@RequiredArgsConstructor
public class WorkflowDefinitionController {

    private final WorkflowDefinitionService workflowDefinitionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkflowDefinitionDto create(@Valid @RequestBody WorkflowDefinitionCreateRequest request) {
        return workflowDefinitionService.create(request);
    }

    @GetMapping("/{definitionId}")
    public WorkflowDefinitionDto getById(@PathVariable UUID definitionId) {
        return workflowDefinitionService.getById(definitionId);
    }

    @GetMapping
    public List<WorkflowDefinitionDto> list(
            @RequestParam EntityType entityType,
            @RequestParam WorkflowAction action) {
        return workflowDefinitionService.list(entityType, action);
    }

    @GetMapping("/active")
    public WorkflowDefinitionDto getActive(
            @RequestParam EntityType entityType,
            @RequestParam WorkflowAction action) {
        return workflowDefinitionService.getActive(entityType, action);
    }

    @PostMapping("/{definitionId}/activate")
    public WorkflowDefinitionDto activate(@PathVariable UUID definitionId) {
        return workflowDefinitionService.activate(definitionId);
    }
}
