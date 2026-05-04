package com.taskflow.collab.repository;

import com.taskflow.collab.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    boolean existsByEventId(String eventId);

    Page<ActivityLog> findByProjectIdOrderByOccurredAtDesc(Long projectId, Pageable pageable);

    @Query(value = """
        SELECT * FROM activity_logs
        WHERE (target_type = 'TASK' AND target_id = :taskId)
           OR (payload->>'task_id' = CAST(:taskId AS TEXT))
        ORDER BY occurred_at DESC
        """,
        countQuery = """
        SELECT COUNT(*) FROM activity_logs
        WHERE (target_type = 'TASK' AND target_id = :taskId)
           OR (payload->>'task_id' = CAST(:taskId AS TEXT))
        """,
        nativeQuery = true)
    Page<ActivityLog> findByTaskId(@Param("taskId") Long taskId, Pageable pageable);
}
