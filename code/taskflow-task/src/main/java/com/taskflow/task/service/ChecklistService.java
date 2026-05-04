package com.taskflow.task.service;

import com.taskflow.common.exception.NotFoundException;
import com.taskflow.task.dto.request.ChecklistItemRequest;
import com.taskflow.task.dto.request.ChecklistRequest;
import com.taskflow.task.dto.response.ChecklistResponse;
import com.taskflow.task.entity.Checklist;
import com.taskflow.task.entity.ChecklistItem;
import com.taskflow.task.entity.Task;
import com.taskflow.task.repository.ChecklistItemRepository;
import com.taskflow.task.repository.ChecklistRepository;
import com.taskflow.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final ChecklistRepository checklistRepository;
    private final ChecklistItemRepository itemRepository;
    private final TaskRepository taskRepository;
    private final AuthorizationService authz;

    @Transactional
    public ChecklistResponse create(Long callerId, Long taskId, ChecklistRequest req) {
        Task t = loadTaskAndAuthz(callerId, taskId);
        Checklist c = new Checklist();
        c.setTaskId(taskId);
        c.setTitle(req.getTitle());
        return toResponse(checklistRepository.save(c), List.of());
    }

    @Transactional
    public void delete(Long callerId, Long checklistId) {
        Checklist c = loadChecklist(checklistId);
        loadTaskAndAuthz(callerId, c.getTaskId());
        c.setDeleted(true);
        checklistRepository.save(c);
    }

    public List<ChecklistResponse> listForTask(Long callerId, Long taskId) {
        loadTaskAndAuthz(callerId, taskId);
        return checklistRepository.findByTaskIdAndDeletedFalse(taskId).stream()
                .map(c -> toResponse(c,
                        itemRepository.findByChecklistIdAndDeletedFalseOrderByPositionAsc(c.getId())))
                .toList();
    }

    @Transactional
    public ChecklistResponse.ItemResponse addItem(Long callerId, Long checklistId, ChecklistItemRequest req) {
        Checklist c = loadChecklist(checklistId);
        loadTaskAndAuthz(callerId, c.getTaskId());

        ChecklistItem it = new ChecklistItem();
        it.setChecklistId(checklistId);
        it.setContent(req.getContent());
        it.setCompleted(req.getCompleted() != null && req.getCompleted());
        it.setPosition(req.getPosition() != null ? req.getPosition() :
                itemRepository.findByChecklistIdAndDeletedFalseOrderByPositionAsc(checklistId).size());
        return toItemResponse(itemRepository.save(it));
    }

    @Transactional
    public ChecklistResponse.ItemResponse updateItem(Long callerId, Long itemId, ChecklistItemRequest req) {
        ChecklistItem it = itemRepository.findByIdAndDeletedFalse(itemId)
                .orElseThrow(() -> NotFoundException.of("ChecklistItem", itemId));
        Checklist c = loadChecklist(it.getChecklistId());
        loadTaskAndAuthz(callerId, c.getTaskId());
        if (req.getContent() != null) it.setContent(req.getContent());
        if (req.getCompleted() != null) it.setCompleted(req.getCompleted());
        if (req.getPosition() != null) it.setPosition(req.getPosition());
        return toItemResponse(itemRepository.save(it));
    }

    @Transactional
    public void deleteItem(Long callerId, Long itemId) {
        ChecklistItem it = itemRepository.findByIdAndDeletedFalse(itemId)
                .orElseThrow(() -> NotFoundException.of("ChecklistItem", itemId));
        Checklist c = loadChecklist(it.getChecklistId());
        loadTaskAndAuthz(callerId, c.getTaskId());
        it.setDeleted(true);
        itemRepository.save(it);
    }

    private Task loadTaskAndAuthz(Long callerId, Long taskId) {
        Task t = taskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> NotFoundException.of("Task", taskId));
        authz.requireRole(t.getProjectId(), callerId, "EDITOR");
        return t;
    }

    private Checklist loadChecklist(Long id) {
        return checklistRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> NotFoundException.of("Checklist", id));
    }

    private ChecklistResponse toResponse(Checklist c, List<ChecklistItem> items) {
        return ChecklistResponse.builder()
                .id(c.getId()).taskId(c.getTaskId()).title(c.getTitle())
                .items(items.stream().map(this::toItemResponse).toList())
                .build();
    }

    private ChecklistResponse.ItemResponse toItemResponse(ChecklistItem it) {
        return ChecklistResponse.ItemResponse.builder()
                .id(it.getId()).checklistId(it.getChecklistId())
                .content(it.getContent()).completed(it.getCompleted())
                .position(it.getPosition()).build();
    }
}
