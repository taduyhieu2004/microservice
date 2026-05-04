package com.taskflow.task.controller;

import com.taskflow.common.dto.ApiResponse;
import com.taskflow.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final TaskRepository taskRepository;

    @GetMapping("/tasks/{id}/exists")
    public ApiResponse<Map<String, Object>> exists(@PathVariable Long id) {
        var task = taskRepository.findByIdAndDeletedFalse(id);
        Map<String, Object> body = new HashMap<>();
        body.put("exists", task.isPresent());
        task.ifPresent(t -> {
            body.put("project_id", t.getProjectId());
            body.put("board_id", t.getBoardId());
            body.put("list_id", t.getListId());
        });
        return ApiResponse.ok(body);
    }
}
