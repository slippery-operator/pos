package com.increff.pos.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.ZonedDateTime;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractPojo {

    @Version
    @Column(nullable = false)
    private Integer version = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
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