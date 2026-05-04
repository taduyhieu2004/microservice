package com.taskflow.project.controller;

import com.taskflow.common.dto.ApiResponse;
import com.taskflow.common.exception.NotFoundException;
import com.taskflow.project.entity.BoardList;
import com.taskflow.project.entity.Board;
import com.taskflow.project.repository.BoardListRepository;
import com.taskflow.project.repository.BoardRepository;
import com.taskflow.project.repository.ProjectRepository;
import com.taskflow.project.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final AuthorizationService authz;
    private final ProjectRepository projectRepository;
    private final BoardRepository boardRepository;
    private final BoardListRepository listRepository;

    @GetMapping("/projects/{id}/members/{userId}/role")
    public ApiResponse<Map<String, Object>> getRole(@PathVariable Long id, @PathVariable Long userId) {
        var role = authz.getRole(id, userId);
        Map<String, Object> body = new HashMap<>();
        body.put("project_id", id);
        body.put("user_id", userId);
        body.put("role", role == null ? null : role.name());
        return ApiResponse.ok(body);
    }

    @GetMapping("/projects/{id}/exists")
    public ApiResponse<Map<String, Boolean>> projectExists(@PathVariable Long id) {
        boolean exists = projectRepository.findByIdAndDeletedFalse(id).isPresent();
        return ApiResponse.ok(Map.of("exists", exists));
    }

    @GetMapping("/lists/{id}/board")
    public ApiResponse<Map<String, Long>> listBoard(@PathVariable Long id) {
        BoardList l = listRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> NotFoundException.of("List", id));
        Board b = boardRepository.findByIdAndDeletedFalse(l.getBoardId())
                .orElseThrow(() -> NotFoundException.of("Board", l.getBoardId()));
        return ApiResponse.ok(Map.of(
                "list_id", id,
                "board_id", b.getId(),
                "project_id", b.getProjectId()));
    }
}
