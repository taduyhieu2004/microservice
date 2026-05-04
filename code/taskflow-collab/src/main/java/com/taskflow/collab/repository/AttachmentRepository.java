package com.taskflow.collab.repository;

import com.taskflow.collab.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    Optional<Attachment> findByIdAndDeletedFalse(Long id);
    List<Attachment> findByTaskIdAndDeletedFalseOrderByCreatedAtDesc(Long taskId);

    @Query("SELECT a FROM Attachment a WHERE a.taskId = :taskId AND a.deleted = false")
    List<Attachment> findActiveByTask(@Param("taskId") Long taskId);

    @Query("SELECT a FROM Attachment a WHERE a.projectId = :projectId AND a.deleted = false")
    List<Attachment> findActiveByProject(@Param("projectId") Long projectId);
}
