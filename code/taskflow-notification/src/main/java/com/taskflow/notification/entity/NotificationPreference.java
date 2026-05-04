package com.taskflow.notification.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

import java.util.Map;

@Data
@Entity
@Table(name = "notification_preferences")
public class NotificationPreference {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "in_app_enabled", nullable = false)
    private Boolean inAppEnabled = true;

    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = false;

    @Type(JsonBinaryType.class)
    @Column(name = "per_type_settings", columnDefinition = "jsonb")
    private Map<String, Boolean> perTypeSettings;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "last_updated_at")
    private Long lastUpdatedAt;
}
