package com.taskflow.task.repository;

import com.taskflow.task.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {
    Optional<Label> findByIdAndDeletedFalse(Long id);
    List<Label> findByProjectIdAndDeletedFalse(Long projectId);
    boolean existsByProjectIdAndNameAndDeletedFalse(Long projectId, String name);
}
