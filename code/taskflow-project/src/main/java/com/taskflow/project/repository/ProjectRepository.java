package com.taskflow.project.repository;

import com.taskflow.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByIdAndDeletedFalse(Long id);

    boolean existsByKeyAndDeletedFalse(String key);

    @Query("""
            SELECT p FROM Project p
            WHERE p.deleted = false
              AND p.id IN (
                  SELECT m.projectId FROM ProjectMember m WHERE m.userId = :userId AND m.deleted = false
              )
            ORDER BY p.createdAt DESC
            """)
    List<Project> findAllForUser(@Param("userId") Long userId);
}
