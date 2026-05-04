package com.taskflow.user.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String bio;
    private LocalDate dob;
    private String status;
    private Long lastLoginAt;
    private Long createdAt;
}
