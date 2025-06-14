package com.real.backend.common.base;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public abstract class BaseEntity {
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
    public void updateCreatedAt(String createdAt) {
        this.createdAt = LocalDateTime.parse(createdAt);
    }

    public void updateUpdatedAt(String updatedAt) {
        this.updatedAt = LocalDateTime.parse(updatedAt);
    }
}
