package com.boatapp.backend.security;

/**
 * Centralises the URL patterns that bypass authentication.
 */
public final class PublicEndpoints {

    /** Endpoints that are always public, regardless of the active profile. */
    public static final String[] ALWAYS = {
            "/actuator/health",
            "/actuator/info"
    };

    /** Endpoints that are public only in non-production profiles (dev / staging). */
    public static final String[] NON_PROD = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

}

