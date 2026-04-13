package com.boatapp.backend.service;

import com.boatapp.backend.dto.BoatRecord;
import com.boatapp.backend.exception.BoatNotFoundException;
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
     * Deletes the boat identified by {@code id}.
     *
     * @param id the identifier of the boat to delete
     * @throws BoatNotFoundException if no boat exists with the given {@code id}
     */
    void deleteBoat(Long id);
}
