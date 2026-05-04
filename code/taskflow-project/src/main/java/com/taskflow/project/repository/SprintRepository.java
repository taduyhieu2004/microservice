package com.taskflow.project.repository;

import com.taskflow.project.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {

    Optional<Sprint> findByIdAndDeletedFalse(Long id);

    List<Sprint> findByProjectIdAndDeletedFalseOrderByStartDateDesc(Long projectId);
}
