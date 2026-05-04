package com.taskflow.task.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TaskResponse {
    private Long id;
    private Long projectId;
    private Long boardId;
    private Long listId;
    private Long sprintId;
    private String title;
    private String description;
    private Long assigneeId;
    private Long reporterId;
    private Long dueDate;
    private String priority;
    private Integer position;
    private Long version;
    private Long createdAt;
    private Long lastUpdatedAt;
    private List<Long> labelIds;
}
