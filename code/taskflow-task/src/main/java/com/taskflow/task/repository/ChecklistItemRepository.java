package com.taskflow.task.repository;

import com.taskflow.task.entity.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {
    Optional<ChecklistItem> findByIdAndDeletedFalse(Long id);
    List<ChecklistItem> findByChecklistIdAndDeletedFalseOrderByPositionAsc(Long checklistId);
}
