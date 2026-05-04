package com.taskflow.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

@EqualsAndHashCode(callSuper = true)
@Data
@MappedSuperclass
public abstract class AuditEntity extends BaseEntity {

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Long createdAt;

    @LastModifiedBy
    @Column(name = "last_updated_by")
    private String lastUpdatedBy;

    @LastModifiedDate
    @Column(name = "last_updated_at")
    private Long lastUpdatedAt;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}
