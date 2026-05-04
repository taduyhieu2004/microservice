package com.taskflow.project.repository;

import com.taskflow.project.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    Optional<ProjectMember> findByProjectIdAndUserIdAndDeletedFalse(Long projectId, Long userId);

    List<ProjectMember> findByProjectIdAndDeletedFalse(Long projectId);

    boolean existsByProjectIdAndUserIdAndDeletedFalse(Long projectId, Long userId);
}
