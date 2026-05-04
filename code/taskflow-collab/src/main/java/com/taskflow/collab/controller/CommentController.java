package com.taskflow.collab.controller;

import com.taskflow.collab.dto.request.CreateCommentRequest;
import com.taskflow.collab.dto.request.UpdateCommentRequest;
import com.taskflow.collab.dto.response.CommentResponse;
import com.taskflow.collab.service.CommentService;
import com.taskflow.common.dto.ApiResponse;
import com.taskflow.common.dto.PageResponse;
import com.taskflow.common.security.SecurityHeaderUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService service;

    @PostMapping("/api/v1/tasks/{taskId}/comments")
    public ApiResponse<CommentResponse> create(@PathVariable Long taskId,
                                               @Valid @RequestBody CreateCommentRequest req) {
        return ApiResponse.created("Comment added",
                service.create(SecurityHeaderUtils.currentUserId(), taskId, req));
    }

    @GetMapping("/api/v1/tasks/{taskId}/comments")
    public ApiResponse<PageResponse<CommentResponse>> list(@PathVariable Long taskId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list(SecurityHeaderUtils.currentUserId(), taskId, page, size));
    }

    @PutMapping("/api/v1/comments/{id}")
    public ApiResponse<CommentResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateCommentRequest req) {
        return ApiResponse.ok("Comment updated",
                service.update(SecurityHeaderUtils.currentUserId(), id, req));
    }

    @DeleteMapping("/api/v1/comments/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(SecurityHeaderUtils.currentUserId(), id);
        return ApiResponse.ok("Comment deleted", null);
    }
}
