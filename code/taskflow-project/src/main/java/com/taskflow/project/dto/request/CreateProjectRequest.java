package com.taskflow.project.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.taskflow.project.constant.enums.ProjectType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateProjectRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Pattern(regexp = "^[A-Z][A-Z0-9]{1,9}$", message = "key_must_be_2_to_10_uppercase")
    private String key;

    @Size(max = 2000)
    private String description;

    private ProjectType type = ProjectType.SOFTWARE;
}
