package com.boatapp.backend.config;

import org.springframework.core.env.Environment;

import java.util.Arrays;

/**
 * Single source of truth for every Spring profile name used in the application.
 *
 */
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

