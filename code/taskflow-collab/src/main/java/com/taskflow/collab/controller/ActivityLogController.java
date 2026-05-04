package com.taskflow.collab.controller;

import com.taskflow.collab.dto.response.ActivityLogResponse;
import com.taskflow.collab.service.ActivityLogService;
import com.taskflow.common.dto.ApiResponse;
import com.taskflow.common.dto.PageResponse;
import com.taskflow.common.security.SecurityHeaderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService service;

    /**
     * Project activity (lưu ý route /api/v1/activities/projects/{id}
     * thay vì /api/v1/projects/{id}/activities để tránh xung đột Gateway routing).
     */
    @GetMapping("/api/v1/activities/projects/{projectId}")
    public ApiResponse<PageResponse<ActivityLogResponse>> projectActivity(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.listForProject(SecurityHeaderUtils.currentUserId(), projectId, page, size));
    }

    @GetMapping("/api/v1/activities/tasks/{taskId}")
    public ApiResponse<PageResponse<ActivityLogResponse>> taskActivity(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.listForTask(SecurityHeaderUtils.currentUserId(), taskId, page, size));
    }
}
