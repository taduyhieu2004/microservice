package com.taskflow.task.entity;

import com.taskflow.common.entity.AuditEntity;
import com.taskflow.task.constant.enums.Priority;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "tasks")
public class Task extends AuditEntity {

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Column(name = "list_id", nullable = false)
    private Long listId;

    @Column(name = "sprint_id")
    private Long sprintId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "assignee_id")
    private Long assigneeId;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(name = "due_date")
    private Long dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;

    @Column(nullable = false)
    private Integer position = 0;

    @Version
    @Column(nullable = false)
    private Long version;
}
