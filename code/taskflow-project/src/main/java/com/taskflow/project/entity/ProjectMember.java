package com.taskflow.project.entity;

import com.taskflow.common.entity.AuditEntity;
import com.taskflow.project.constant.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "project_members",
        uniqueConstraints = @UniqueConstraint(name = "uq_member_project_user", columnNames = {"project_id", "user_id"}))
public class ProjectMember extends AuditEntity {

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "joined_at")
    private Long joinedAt;
}
