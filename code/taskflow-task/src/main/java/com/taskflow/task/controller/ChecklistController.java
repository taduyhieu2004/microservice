package com.taskflow.task.controller;

import com.taskflow.common.dto.ApiResponse;
import com.taskflow.common.security.SecurityHeaderUtils;
import com.taskflow.task.dto.request.ChecklistItemRequest;
import com.taskflow.task.dto.request.ChecklistRequest;
import com.taskflow.task.dto.response.ChecklistResponse;
import com.taskflow.task.service.ChecklistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChecklistController {

    private final ChecklistService service;

    @PostMapping("/api/v1/tasks/{taskId}/checklists")
    public ApiResponse<ChecklistResponse> create(@PathVariable Long taskId,
                                                 @Valid @RequestBody ChecklistRequest req) {
        return ApiResponse.created("Checklist created",
                service.create(SecurityHeaderUtils.currentUserId(), taskId, req));
    }

    @GetMapping("/api/v1/tasks/{taskId}/checklists")
    public ApiResponse<List<ChecklistResponse>> list(@PathVariable Long taskId) {
        return ApiResponse.ok(service.listForTask(SecurityHeaderUtils.currentUserId(), taskId));
    }

    @DeleteMapping("/api/v1/checklists/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(SecurityHeaderUtils.currentUserId(), id);
        return ApiResponse.ok("Checklist deleted", null);
    }

    @PostMapping("/api/v1/checklists/{id}/items")
    public ApiResponse<ChecklistResponse.ItemResponse> addItem(@PathVariable Long id,
                                                               @Valid @RequestBody ChecklistItemRequest req) {
        return ApiResponse.created("Item added",
                service.addItem(SecurityHeaderUtils.currentUserId(), id, req));
    }

    @PatchMapping("/api/v1/checklist-items/{id}")
    public ApiResponse<ChecklistResponse.ItemResponse> updateItem(@PathVariable Long id,
                                                                  @Valid @RequestBody ChecklistItemRequest req) {
        return ApiResponse.ok("Item updated",
                service.updateItem(SecurityHeaderUtils.currentUserId(), id, req));
    }

    @DeleteMapping("/api/v1/checklist-items/{id}")
    public ApiResponse<Void> deleteItem(@PathVariable Long id) {
        service.deleteItem(SecurityHeaderUtils.currentUserId(), id);
        return ApiResponse.ok("Item deleted", null);
    }
}
