package com.taskflow.task.controller;

import com.taskflow.common.dto.ApiResponse;
import com.taskflow.common.security.SecurityHeaderUtils;
import com.taskflow.task.dto.request.LabelRequest;
import com.taskflow.task.dto.response.LabelResponse;
import com.taskflow.task.service.LabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * URL pattern dùng /api/v1/labels (không nest dưới /projects)
 * vì Gateway route /api/v1/projects/** → Project Service.
 */
@RestController
@RequestMapping("/api/v1/labels")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @GetMapping
    public ApiResponse<List<LabelResponse>> list(@RequestParam("project_id") Long projectId) {
        return ApiResponse.ok(labelService.list(SecurityHeaderUtils.currentUserId(), projectId));
    }

    @PostMapping
    public ApiResponse<LabelResponse> create(@RequestParam("project_id") Long projectId,
                                             @Valid @RequestBody LabelRequest req) {
        return ApiResponse.created("Label created",
                labelService.create(SecurityHeaderUtils.currentUserId(), projectId, req));
    }

    @PutMapping("/{id}")
    public ApiResponse<LabelResponse> update(@PathVariable Long id, @Valid @RequestBody LabelRequest req) {
        return ApiResponse.ok("Label updated",
                labelService.update(SecurityHeaderUtils.currentUserId(), id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        labelService.delete(SecurityHeaderUtils.currentUserId(), id);
        return ApiResponse.ok("Label deleted", null);
    }
}
