package com.boatapp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a new {@link com.boatapp.backend.entity.Boat}.
 *
 * <p>Validated at the controller layer via {@code @Valid}.
 */
public record BoatRequest(

        @NotBlank
        @Size(max = 30)
        @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Name can only contain letters, numbers and hyphens")
        String name,

        @Size(max = 500)
        String description
) {}

