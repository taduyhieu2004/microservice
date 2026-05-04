package com.taskflow.task.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
@Entity
@Table(name = "task_label_mappings")
@IdClass(TaskLabelMapping.PK.class)
public class TaskLabelMapping {

    @Id
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Id
    @Column(name = "label_id", nullable = false)
    private Long labelId;

    @Data
    public static class PK implements Serializable {
        private Long taskId;
        private Long labelId;

        public PK() {}
        public PK(Long taskId, Long labelId) {
            this.taskId = taskId;
            this.labelId = labelId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK pk)) return false;
            return Objects.equals(taskId, pk.taskId) && Objects.equals(labelId, pk.labelId);
        }
        @Override
        public int hashCode() { return Objects.hash(taskId, labelId); }
    }
}
