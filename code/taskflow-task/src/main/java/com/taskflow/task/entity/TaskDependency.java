package com.taskflow.task.entity;

import com.taskflow.common.entity.AuditEntity;
import com.taskflow.task.constant.enums.DependencyType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "task_dependencies",
        uniqueConstraints = @UniqueConstraint(columnNames = {"task_id","depends_on_task_id"}))
public class TaskDependency extends AuditEntity {

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "depends_on_task_id", nullable = false)
    private Long dependsOnTaskId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DependencyType type = DependencyType.BLOCKS;
}
