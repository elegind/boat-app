package com.boatapp.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Base class for all auditable JPA entities.
 *
 * <p>Requires {@code @EnableJpaAuditing} on the application or a
 * {@code @Configuration} class (already present on {@code BoatAppApplication}).
 *
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class Auditable {

    /**
     * UTC timestamp set automatically when the entity is first persisted.
     * {@code updatable = false} constraint ensures it is never overwritten.
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

}

