package com.taskflow.task.entity;

import com.taskflow.common.entity.AuditEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "labels", uniqueConstraints = @UniqueConstraint(columnNames = {"project_id","name"}))
public class Label extends AuditEntity {
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 7)
    private String color;
}
