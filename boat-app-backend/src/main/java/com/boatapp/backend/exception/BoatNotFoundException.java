package com.boatapp.backend.exception;

/**
 * <p>Caught by {@link com.boatapp.backend.controller.GlobalExceptionHandler}
 * and mapped to a {@code 404 Not Found} response.
 */
public class BoatNotFoundException extends RuntimeException {

    /**
     * @param id the identifier of the missing boat
     */
    public BoatNotFoundException(Long id) {
        super("Boat not found with id: " + id);
    }
}

