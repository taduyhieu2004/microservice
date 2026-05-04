package com.taskflow.task.repository;

import com.taskflow.task.entity.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {
    Optional<TaskDependency> findByIdAndDeletedFalse(Long id);
    List<TaskDependency> findByTaskIdAndDeletedFalse(Long taskId);

    boolean existsByTaskIdAndDependsOnTaskIdAndDeletedFalse(Long taskId, Long dependsOnTaskId);
}
