package com.taskflow.user.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateProfileRequest {

    @Size(max = 255)
    private String fullName;

    @Size(max = 512)
    private String avatarUrl;

    @Size(max = 1000)
    private String bio;

    private LocalDate dob;
}
