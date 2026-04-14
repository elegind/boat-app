package com.boatapp.backend.security;

import org.springframework.http.HttpMethod;

/**
 * Central permission matrix for the Boat API.
 *
 * <p>Each constant binds a required role, an HTTP method and a URL pattern.
 */
public enum Permission {

    BOATS_READ  ("ROLE_USER",  HttpMethod.GET,    "/api/v1/boats/**"),
    BOATS_CREATE("ROLE_ADMIN", HttpMethod.POST,   "/api/v1/boats"),
    BOATS_UPDATE("ROLE_ADMIN", HttpMethod.PUT,    "/api/v1/boats/**"),
    BOATS_DELETE("ROLE_ADMIN", HttpMethod.DELETE, "/api/v1/boats/**");

    /** Spring Security authority string (e.g. {@code ROLE_USER}). */
    public final String role;

    /** HTTP method this permission applies to. */
    public final HttpMethod method;

    /** Ant-style URL pattern. */
    public final String pattern;

    Permission(String role, HttpMethod method, String pattern) {
        this.role    = role;
        this.method  = method;
        this.pattern = pattern;
    }
}

