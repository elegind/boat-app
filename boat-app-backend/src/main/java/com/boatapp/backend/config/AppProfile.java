package com.boatapp.backend.config;

import lombok.Getter;
import org.springframework.core.env.Environment;

import java.util.Arrays;

/**
 * Single source of truth for every Spring profile name used in the application.
 *
 * <p>Use {@link AppProfile#DEV} / {@link AppProfile#PROD} in calls to
 * {@link #isActive(Environment)} to avoid hardcoding profile name strings.
 *
 * <pre>{@code
 * AppProfile.DEV.isActive(environment)
 * }</pre>
 */
@Getter
public enum AppProfile {

    DEV("dev"),
    PROD("prod");

    private final String value;

    AppProfile(String value) {
        this.value = value;
    }

    /**
     * Returns {@code true} when this profile is among the currently active ones.
     */
    public boolean isActive(Environment environment) {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(value::equalsIgnoreCase);
    }
}

