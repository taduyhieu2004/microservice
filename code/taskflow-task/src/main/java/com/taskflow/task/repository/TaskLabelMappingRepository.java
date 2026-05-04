package com.taskflow.task.repository;

import com.taskflow.task.entity.TaskLabelMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskLabelMappingRepository extends JpaRepository<TaskLabelMapping, TaskLabelMapping.PK> {
    List<TaskLabelMapping> findByTaskId(Long taskId);

    @Modifying
    @Query("DELETE FROM TaskLabelMapping m WHERE m.taskId = :taskId")
    void deleteByTaskId(@Param("taskId") Long taskId);
}
