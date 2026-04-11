package com.boatapp.backend.controller;

import com.boatapp.backend.service.IBoatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing Boat resources for API <b>version 1</b>.
 *
 * <p>The class is intentionally suffixed {@code V1} so that a future
 * {@code BoatControllerV2} can coexist in the same codebase without
 * any naming ambiguity.
 *
 */
@RestController
@RequestMapping("/api/v1/boats")
public class BoatControllerV1 {

    private final IBoatService boatService;

    public BoatControllerV1(IBoatService boatService) {
        this.boatService = boatService;
    }

    /**
     * Test endpoint, will be removed later
     *
     * @return 200 OK with plain-text greeting
     */
    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello from BoatController");
    }
}

