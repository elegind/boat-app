package com.boatapp.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * JPA entity representing a Boat.
 *
 * <p>Extends {@link Auditable} to inherit {@code createdAt}
 * audit timestamps, automatically managed by Spring Data JPA.
 *
 */
@Entity
@Table(name = "boat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Boat extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "boat_seq")
    @SequenceGenerator(name = "boat_seq", sequenceName = "boat_seq", allocationSize = 50)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String name;

    private String description;
}
