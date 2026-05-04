package com.taskflow.project.controller;

import com.taskflow.common.dto.ApiResponse;
import com.taskflow.common.security.SecurityHeaderUtils;
import com.taskflow.project.dto.request.BoardRequest;
import com.taskflow.project.dto.request.ListRequest;
import com.taskflow.project.dto.request.ReorderListsRequest;
import com.taskflow.project.dto.response.BoardResponse;
import com.taskflow.project.dto.response.ListResponse;
import com.taskflow.project.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // ===== Board =====
    @PostMapping("/api/v1/projects/{projectId}/boards")
    public ApiResponse<BoardResponse> create(@PathVariable Long projectId,
                                             @Valid @RequestBody BoardRequest req) {
        return ApiResponse.created("Board created",
                boardService.create(SecurityHeaderUtils.currentUserId(), projectId, req));
    }

    @GetMapping("/api/v1/projects/{projectId}/boards")
    public ApiResponse<List<BoardResponse>> list(@PathVariable Long projectId) {
        return ApiResponse.ok(boardService.listForProject(SecurityHeaderUtils.currentUserId(), projectId));
    }

    @GetMapping("/api/v1/boards/{id}")
    public ApiResponse<BoardResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(boardService.get(SecurityHeaderUtils.currentUserId(), id));
    }

    @PutMapping("/api/v1/boards/{id}")
    public ApiResponse<BoardResponse> update(@PathVariable Long id, @Valid @RequestBody BoardRequest req) {
        return ApiResponse.ok("Board updated",
                boardService.update(SecurityHeaderUtils.currentUserId(), id, req));
    }

    @DeleteMapping("/api/v1/boards/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        boardService.delete(SecurityHeaderUtils.currentUserId(), id);
        return ApiResponse.ok("Board deleted", null);
    }

    // ===== List (Kanban column) =====
    @PostMapping("/api/v1/boards/{boardId}/lists")
    public ApiResponse<ListResponse> createList(@PathVariable Long boardId,
                                                @Valid @RequestBody ListRequest req) {
        return ApiResponse.created("List created",
                boardService.createList(SecurityHeaderUtils.currentUserId(), boardId, req));
    }

    @PutMapping("/api/v1/lists/{id}")
    public ApiResponse<ListResponse> updateList(@PathVariable Long id, @Valid @RequestBody ListRequest req) {
        return ApiResponse.ok("List updated",
                boardService.updateList(SecurityHeaderUtils.currentUserId(), id, req));
    }

    @DeleteMapping("/api/v1/lists/{id}")
    public ApiResponse<Void> deleteList(@PathVariable Long id) {
        boardService.deleteList(SecurityHeaderUtils.currentUserId(), id);
        return ApiResponse.ok("List deleted", null);
    }

    @PatchMapping("/api/v1/boards/{boardId}/lists/reorder")
    public ApiResponse<List<ListResponse>> reorder(@PathVariable Long boardId,
                                                   @Valid @RequestBody ReorderListsRequest req) {
        return ApiResponse.ok("Reordered",
                boardService.reorderLists(SecurityHeaderUtils.currentUserId(), boardId, req));
    }
}
