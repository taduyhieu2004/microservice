package com.taskflow.user.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RegisterRequest {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,64}$", message = "username_invalid_format")
    private String username;

    @NotBlank
    @Email(message = "email_invalid_format")
    private String email;

    @NotBlank
    @Size(min = 6, max = 100, message = "password_invalid_length")
    private String password;

    @Size(max = 255)
    private String fullName;
}
