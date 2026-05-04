package com.taskflow.project.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.taskflow.project.constant.enums.SprintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SprintRequest {
    @NotBlank @Size(max = 255)
    private String name;

    @Size(max = 1000)
    private String goal;

    private Long startDate;
    private Long endDate;
    private SprintStatus status;
}
