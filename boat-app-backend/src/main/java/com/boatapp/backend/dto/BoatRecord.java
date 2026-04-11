package com.boatapp.backend.dto;

import java.time.Instant;

/**
 * Immutable DTO representing a Boat resource exposed via the API.
 */
public record BoatRecord(
        Long id,
        String name,
        String description,
        Instant createdAt
) {}
