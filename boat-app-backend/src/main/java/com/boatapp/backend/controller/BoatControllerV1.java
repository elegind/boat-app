package com.boatapp.backend.controller;

import com.boatapp.backend.dto.BoatRecord;
import com.boatapp.backend.service.IBoatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing Boat resources for API <b>version 1</b>.
 *
 * <p>The class is intentionally suffixed {@code V1} so that a future
 * {@code BoatControllerV2} can coexist in the same codebase without
 * any naming ambiguity.
 */
@Tag(name = "Boats", description = "Manage the fleet of boats")
@RestController
@RequestMapping("/api/v1/boats")
@Validated
public class BoatControllerV1 {

    private final IBoatService boatService;

    public BoatControllerV1(IBoatService boatService) {
        this.boatService = boatService;
    }

    /**
     * Returns a paginated list of boats, sorted by creation date descending.
     *
     * @param page zero-based page index (default: 0)
     * @param size page size (default: 5)
     * @return 200 OK with a {@link Page} of {@link BoatRecord}
     */
    @Operation(
            summary = "List boats (paginated)",
            description = "Returns a page of boats sorted by creation date descending."
    )
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = BoatRecord.class)))
    @GetMapping
    public ResponseEntity<Page<BoatRecord>> getAllBoats(
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page must be >= 0") int page,
            @Parameter(description = "Page size", example = "5")
            @RequestParam(defaultValue = "5")
            @Min(value = 1, message = "size must be >= 1") int size) {
        return ResponseEntity.ok(boatService.findAll(page, size));
    }

}
