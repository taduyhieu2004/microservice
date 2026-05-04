package com.taskflow.task.repository;

import com.taskflow.task.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChecklistRepository extends JpaRepository<Checklist, Long> {
    Optional<Checklist> findByIdAndDeletedFalse(Long id);
    List<Checklist> findByTaskIdAndDeletedFalse(Long taskId);
}
