package com.boatapp.backend.mapper;

import com.boatapp.backend.dto.BoatRecord;
import com.boatapp.backend.entity.Boat;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper between {@link Boat} entity and {@link BoatRecord} DTO.
 */
@Mapper(componentModel = "spring")
public interface BoatMapper {

    BoatRecord toRecord(Boat boat);

    List<BoatRecord> toRecordList(List<Boat> boats);
}

