package com.taskflow.task.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.taskflow.task.constant.enums.Priority;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateTaskRequest {
    @Size(max = 500)
    private String title;

    @Size(max = 5000)
    private String description;

    private Long assigneeId;
    private Long dueDate;
    private Priority priority;

    private List<Long> labelIds;

    private Long sprintId;
}
