package com.taskflow.project.entity;

import com.taskflow.common.entity.AuditEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "board_lists")
public class BoardList extends AuditEntity {

    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer position = 0;
}
