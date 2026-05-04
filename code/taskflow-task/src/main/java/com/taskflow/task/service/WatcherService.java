package com.taskflow.task.service;

import com.taskflow.common.exception.NotFoundException;
import com.taskflow.task.entity.Task;
import com.taskflow.task.entity.TaskWatcher;
import com.taskflow.task.repository.TaskRepository;
import com.taskflow.task.repository.TaskWatcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class WatcherService {

    private final TaskWatcherRepository repo;
    private final TaskRepository taskRepository;
    private final AuthorizationService authz;

    @Transactional
    public void watch(Long callerId, Long taskId) {
        Task t = loadTaskAndAuth(callerId, taskId);
        TaskWatcher w = new TaskWatcher();
        w.setTaskId(taskId);
        w.setUserId(callerId);
        w.setWatchedAt(Instant.now().toEpochMilli());
        repo.save(w);
    }

    @Transactional
    public void unwatch(Long callerId, Long taskId) {
        loadTaskAndAuth(callerId, taskId);
        repo.deleteById(new TaskWatcher.PK(taskId, callerId));
    }

    private Task loadTaskAndAuth(Long callerId, Long taskId) {
        Task t = taskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> NotFoundException.of("Task", taskId));
        authz.requireMember(t.getProjectId(), callerId);
        return t;
    }
}
