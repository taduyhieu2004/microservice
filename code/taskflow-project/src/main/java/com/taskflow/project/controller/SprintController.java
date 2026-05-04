package com.taskflow.project.controller;

import com.taskflow.common.dto.ApiResponse;
import com.taskflow.common.security.SecurityHeaderUtils;
import com.taskflow.project.dto.request.SprintRequest;
import com.taskflow.project.dto.response.SprintResponse;
import com.taskflow.project.service.SprintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    @PostMapping("/api/v1/projects/{projectId}/sprints")
    public ApiResponse<SprintResponse> create(@PathVariable Long projectId,
                                              @Valid @RequestBody SprintRequest req) {
        return ApiResponse.created("Sprint created",
                sprintService.create(SecurityHeaderUtils.currentUserId(), projectId, req));
    }

    @GetMapping("/api/v1/projects/{projectId}/sprints")
    public ApiResponse<List<SprintResponse>> list(@PathVariable Long projectId) {
        return ApiResponse.ok(sprintService.listForProject(SecurityHeaderUtils.currentUserId(), projectId));
    }

    @PatchMapping("/api/v1/sprints/{id}")
    public ApiResponse<SprintResponse> update(@PathVariable Long id, @Valid @RequestBody SprintRequest req) {
        return ApiResponse.ok("Sprint updated",
                sprintService.update(SecurityHeaderUtils.currentUserId(), id, req));
    }
}
