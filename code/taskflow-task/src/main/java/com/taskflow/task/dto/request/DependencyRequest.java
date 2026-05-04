package com.taskflow.task.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.taskflow.task.constant.enums.DependencyType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DependencyRequest {
    @NotNull
    private Long dependsOnTaskId;
    private DependencyType type;
}
