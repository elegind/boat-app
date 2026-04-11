package com.boatapp.backend.controller;

import com.boatapp.backend.config.AppProfile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Centralised exception handling for all REST controllers.
 *
 * <p>Every exception is mapped to a consistent {@link ErrorResponse} JSON body:
 * <pre>
 * {
 *   "status":    404,
 *   "error":     "Not Found",
 *   "message":   "Boat not found with id: 42",
 *   "timestamp": "2025-01-01T12:00:00"
 * }
 * </pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Environment environment;

    public GlobalExceptionHandler(Environment environment) {
        this.environment = environment;
    }

    /**
     * Re-uses the status and reason already embedded in {@link ResponseStatusException}.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        int statusValue = ex.getStatusCode().value();
        String httpPhrase = resolvePhrase(statusValue);
        String message = ex.getReason() != null ? ex.getReason() : httpPhrase;

        return ResponseEntity.status(ex.getStatusCode())
                .body(new ErrorResponse(statusValue, httpPhrase, message, Instant.now()));
    }

    /**
     * Bean-validation failures — returns 400 with a comma-separated list of field errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), message, Instant.now()));
    }

    /**
     * Catch-all fallback.
     *
     * <ul>
     *   <li><b>{@link AppProfile#DEV}</b> — exposes the actual exception message to speed up debugging.</li>
     *   <li><b>other profiles</b> — returns a generic message; details stay in server logs only.</li>
     * </ul>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        String message = AppProfile.DEV.isActive(environment)
                ? ex.getMessage()
                : "An unexpected error occurred. Please contact support.";

        return ResponseEntity.internalServerError()
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), message, Instant.now()));
    }


    private String resolvePhrase(int statusValue) {
        try {
            return HttpStatus.valueOf(statusValue).getReasonPhrase();
        } catch (IllegalArgumentException ignored) {
            return "Error";
        }
    }

    /**
     * Uniform error envelope returned by every handler in this advice.
     */
    public record ErrorResponse(
            int status,
            String error,
            String message,
            Instant timestamp
    ) {}
}


