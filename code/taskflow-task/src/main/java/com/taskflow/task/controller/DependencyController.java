package com.taskflow.task.controller;

import com.taskflow.common.dto.ApiResponse;
import com.taskflow.common.security.SecurityHeaderUtils;
import com.taskflow.task.dto.request.DependencyRequest;
import com.taskflow.task.dto.response.DependencyResponse;
import com.taskflow.task.service.DependencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DependencyController {

    private final DependencyService service;

    @GetMapping("/api/v1/tasks/{taskId}/dependencies")
    public ApiResponse<List<DependencyResponse>> list(@PathVariable Long taskId) {
        return ApiResponse.ok(service.list(SecurityHeaderUtils.currentUserId(), taskId));
    }

    @PostMapping("/api/v1/tasks/{taskId}/dependencies")
    public ApiResponse<DependencyResponse> add(@PathVariable Long taskId,
                                               @Valid @RequestBody DependencyRequest req) {
        return ApiResponse.created("Dependency added",
                service.add(SecurityHeaderUtils.currentUserId(), taskId, req));
    }

    @DeleteMapping("/api/v1/tasks/{taskId}/dependencies/{depId}")
    public ApiResponse<Void> remove(@PathVariable Long taskId, @PathVariable Long depId) {
        service.remove(SecurityHeaderUtils.currentUserId(), taskId, depId);
        return ApiResponse.ok("Dependency removed", null);
    }
}
