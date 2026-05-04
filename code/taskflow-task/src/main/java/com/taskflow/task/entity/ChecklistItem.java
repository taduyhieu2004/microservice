package com.taskflow.task.entity;

import com.taskflow.common.entity.AuditEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "checklist_items")
public class ChecklistItem extends AuditEntity {
    @Column(name = "checklist_id", nullable = false)
    private Long checklistId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(nullable = false)
    private Integer position = 0;
}
