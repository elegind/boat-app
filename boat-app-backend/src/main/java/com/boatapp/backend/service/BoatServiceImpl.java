package com.boatapp.backend.service;

import com.boatapp.backend.dto.BoatRecord;
import com.boatapp.backend.mapper.BoatMapper;
import com.boatapp.backend.repository.BoatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link IBoatService}.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class BoatServiceImpl implements IBoatService {

    private final BoatRepository boatRepository;
    private final BoatMapper boatMapper;

    public BoatServiceImpl(BoatRepository boatRepository, BoatMapper boatMapper) {
        this.boatRepository = boatRepository;
        this.boatMapper = boatMapper;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link BoatRepository#findAll(Pageable)} and maps each
     * {@code Boat} entity to a {@link BoatRecord} DTO via {@link BoatMapper}.
     */
    @Override
    public Page<BoatRecord> findAll(int page, int size) {
        log.debug("findAll called — page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return boatRepository.findAll(pageable).map(boatMapper::toRecord);
    }
}
