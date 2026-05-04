package com.taskflow.user.entity;

import com.taskflow.common.entity.AuditEntity;
import com.taskflow.user.constant.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "users")
public class User extends AuditEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "last_login_at")
    private Long lastLoginAt;

    @Column(name = "password_expired_at")
    private Long passwordExpiredAt;

    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts = 0;
}
