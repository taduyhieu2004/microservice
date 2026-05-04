package com.taskflow.task.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
@Entity
@Table(name = "task_watchers")
@IdClass(TaskWatcher.PK.class)
public class TaskWatcher {

    @Id
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "watched_at")
    private Long watchedAt;

    @Data
    public static class PK implements Serializable {
        private Long taskId;
        private Long userId;

        public PK() {}
        public PK(Long taskId, Long userId) {
            this.taskId = taskId;
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK p)) return false;
            return Objects.equals(taskId, p.taskId) && Objects.equals(userId, p.userId);
        }
        @Override
        public int hashCode() { return Objects.hash(taskId, userId); }
    }
}
