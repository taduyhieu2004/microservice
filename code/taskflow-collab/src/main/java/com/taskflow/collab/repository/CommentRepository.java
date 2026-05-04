package com.taskflow.collab.repository;

import com.taskflow.collab.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByIdAndDeletedFalse(Long id);

    Page<Comment> findByTaskIdAndDeletedFalseOrderByCreatedAtAsc(Long taskId, Pageable pageable);

    @Modifying
    @Query("UPDATE Comment c SET c.deleted = true WHERE c.taskId = :taskId AND c.deleted = false")
    int softDeleteByTask(@Param("taskId") Long taskId);

    @Modifying
    @Query("UPDATE Comment c SET c.deleted = true WHERE c.projectId = :projectId AND c.deleted = false")
    int softDeleteByProject(@Param("projectId") Long projectId);
}
