package com.taskflow.user.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LoginResponse {
    private Long id;
    private String username;
    private String accessToken;
    private String refreshToken;
    private long tokenExpiredSeconds;
    private long refreshExpiredSeconds;
    @Builder.Default
    private String tokenType = "Bearer";
}
