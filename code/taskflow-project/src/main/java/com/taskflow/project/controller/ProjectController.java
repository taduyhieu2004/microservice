package com.taskflow.project.controller;

import com.taskflow.common.dto.ApiResponse;
import com.taskflow.common.security.SecurityHeaderUtils;
import com.taskflow.project.dto.request.CreateProjectRequest;
import com.taskflow.project.dto.request.UpdateProjectRequest;
import com.taskflow.project.dto.response.ProjectResponse;
import com.taskflow.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ApiResponse<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest req) {
        return ApiResponse.created("Project created",
                projectService.create(SecurityHeaderUtils.currentUserId(), req));
    }

    @GetMapping
    public ApiResponse<List<ProjectResponse>> listMine() {
        return ApiResponse.ok(projectService.listMine(SecurityHeaderUtils.currentUserId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProjectResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(projectService.get(SecurityHeaderUtils.currentUserId(), id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProjectResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateProjectRequest req) {
        return ApiResponse.ok("Project updated",
                projectService.update(SecurityHeaderUtils.currentUserId(), id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        projectService.delete(SecurityHeaderUtils.currentUserId(), id);
        return ApiResponse.ok("Project deleted", null);
    }
}
