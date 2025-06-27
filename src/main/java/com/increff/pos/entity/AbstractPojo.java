package com.increff.pos.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.ZonedDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractPojo {

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now().withZoneSameInstant(java.time.ZoneOffset.UTC);
        }
        if (updatedAt == null) {
            updatedAt = ZonedDateTime.now().withZoneSameInstant(java.time.ZoneOffset.UTC);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now().withZoneSameInstant(java.time.ZoneOffset.UTC);
    }
}