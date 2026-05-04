package com.taskflow.user.controller;

import com.taskflow.common.dto.ApiResponse;
import com.taskflow.common.dto.PageResponse;
import com.taskflow.common.security.SecurityHeaderUtils;
import com.taskflow.user.dto.request.UpdateProfileRequest;
import com.taskflow.user.dto.response.UserResponse;
import com.taskflow.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.ok(userService.getById(SecurityHeaderUtils.currentUserId()));
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok("Profile updated",
                userService.updateProfile(SecurityHeaderUtils.currentUserId(), request));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(userService.getById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> search(@RequestParam("q") String q,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(userService.search(q, page, size));
    }
}
