package com.boatapp.backend.service;

import com.boatapp.backend.dto.BoatRecord;
import com.boatapp.backend.dto.BoatRequest;
import com.boatapp.backend.exception.BoatNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;

/**
 * Contract for the Boat business-logic layer
 */
public interface IBoatService {

    /**
     * Returns a paginated list of boats sorted by creation date descending
     *
     * @param page zero-based page index
     * @param size number of records per page
     * @return a {@link Page} of {@link BoatRecord}
     */
    Page<BoatRecord> findAll(int page, int size);

    /**
     * Creates a new boat from the given request payload.
     *
     * @param request the validated creation payload
     * @return the persisted boat as a {@link BoatRecord}
     */
    BoatRecord createBoat(@NotNull @Valid BoatRequest request);

    /**
     * Deletes the boat identified by {@code id}.
     *
     * @param id the identifier of the boat to delete
     * @throws BoatNotFoundException if no boat exists with the given {@code id}
     */
    void deleteBoat(@NotNull Long id);

    /**
     * Updates the name and description of the boat identified by {@code id}.
     *
     * @param id      the identifier of the boat to update
     * @param request the validated update payload
     * @return the updated boat as a {@link BoatRecord}
     * @throws BoatNotFoundException if no boat exists with the given {@code id}
     */
    BoatRecord updateBoat(@NotNull Long id, @NotNull @Valid BoatRequest request);
}
