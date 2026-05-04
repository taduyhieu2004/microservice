package com.taskflow.project.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.taskflow.project.constant.enums.ProjectType;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateProjectRequest {

    @Size(max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

    private ProjectType type;
}
