package com.taskflow.project.entity;

import com.taskflow.common.entity.AuditEntity;
import com.taskflow.project.constant.enums.ProjectType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "projects")
public class Project extends AuditEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "key", nullable = false, unique = true, length = 10)
    private String key;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectType type = ProjectType.SOFTWARE;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
}
