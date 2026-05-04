package com.taskflow.user.mapper;

import com.taskflow.user.dto.response.UserContactResponse;
import com.taskflow.user.dto.response.UserResponse;
import com.taskflow.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "status", target = "status")
    UserResponse toResponse(User user);

    UserContactResponse toContact(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "failedAttempts", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "passwordExpiredAt", ignore = true)
    void updateProfile(com.taskflow.user.dto.request.UpdateProfileRequest req, @MappingTarget User user);

    default String map(com.taskflow.user.constant.enums.UserStatus s) {
        return s == null ? null : s.name();
    }
}
