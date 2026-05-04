package com.taskflow.task.controller;

import com.taskflow.common.dto.ApiResponse;
import com.taskflow.common.security.SecurityHeaderUtils;
import com.taskflow.task.service.WatcherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WatcherController {

    private final WatcherService service;

    @PostMapping("/api/v1/tasks/{id}/watch")
    public ApiResponse<Void> watch(@PathVariable Long id) {
        service.watch(SecurityHeaderUtils.currentUserId(), id);
        return ApiResponse.ok("Watching", null);
    }

    @DeleteMapping("/api/v1/tasks/{id}/watch")
    public ApiResponse<Void> unwatch(@PathVariable Long id) {
        service.unwatch(SecurityHeaderUtils.currentUserId(), id);
        return ApiResponse.ok("Stopped watching", null);
    }
}
