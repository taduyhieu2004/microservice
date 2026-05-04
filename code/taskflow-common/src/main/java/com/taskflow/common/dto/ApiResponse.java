package com.taskflow.common.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ApiResponse<T> {

    private int status;
    private String message;
    private T data;
    private String timestamp;

    public static <T> ApiResponse<T> ok(String message, T data) {
        return of(HttpStatus.OK.value(), message, data, Instant.now().toString());
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ok("Success", data);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return of(HttpStatus.CREATED.value(), message, data, Instant.now().toString());
    }

    public static <T> ApiResponse<T> error(int status, String message, T data) {
        return of(status, message, data, Instant.now().toString());
    }
}
