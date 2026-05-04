package com.taskflow.task.repository;

import com.taskflow.task.entity.TaskWatcher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskWatcherRepository extends JpaRepository<TaskWatcher, TaskWatcher.PK> {
    List<TaskWatcher> findByTaskId(Long taskId);
}
