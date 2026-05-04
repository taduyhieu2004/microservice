package com.taskflow.notification.controller;

import com.taskflow.common.dto.ApiResponse;
import com.taskflow.common.dto.PageResponse;
import com.taskflow.common.security.SecurityHeaderUtils;
import com.taskflow.notification.dto.request.UpdatePreferenceRequest;
import com.taskflow.notification.dto.response.NotificationResponse;
import com.taskflow.notification.dto.response.PreferenceResponse;
import com.taskflow.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> list(
            @RequestParam(value = "unread_only", defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list(SecurityHeaderUtils.currentUserId(), unreadOnly, page, size));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount() {
        return ApiResponse.ok(Map.of("count", service.unreadCount(SecurityHeaderUtils.currentUserId())));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markRead(@PathVariable Long id) {
        return ApiResponse.ok(service.markRead(SecurityHeaderUtils.currentUserId(), id));
    }

    @PatchMapping("/read-all")
    public ApiResponse<Map<String, Integer>> markAllRead() {
        return ApiResponse.ok(Map.of("updated", service.markAllRead(SecurityHeaderUtils.currentUserId())));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(SecurityHeaderUtils.currentUserId(), id);
        return ApiResponse.ok("Deleted", null);
    }

    @GetMapping("/preferences")
    public ApiResponse<PreferenceResponse> getPreference() {
        return ApiResponse.ok(service.getPreference(SecurityHeaderUtils.currentUserId()));
    }

    @PutMapping("/preferences")
    public ApiResponse<PreferenceResponse> updatePreference(@RequestBody UpdatePreferenceRequest req) {
        return ApiResponse.ok("Preference updated",
                service.updatePreference(SecurityHeaderUtils.currentUserId(), req));
    }
}
