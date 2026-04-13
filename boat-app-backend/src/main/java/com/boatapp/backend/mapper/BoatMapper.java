package com.boatapp.backend.mapper;

import com.boatapp.backend.dto.BoatRecord;
import com.boatapp.backend.dto.BoatRequest;
import com.boatapp.backend.entity.Boat;
import org.mapstruct.Mapper;


/**
 * MapStruct mapper between {@link Boat} entity and its DTOs.
 */
@Mapper(componentModel = "spring")
public interface BoatMapper {

    BoatRecord toRecord(Boat boat);

    /**
     * Maps a {@link BoatRequest} to a new {@link Boat} entity.
     * The {@code id} and {@code createdAt} fields are left unmapped
     * and will be set by JPA / Spring Data auditing on save.
     */
    Boat toEntity(BoatRequest request);
}

