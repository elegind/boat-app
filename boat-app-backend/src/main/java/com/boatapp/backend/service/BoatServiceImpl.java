package com.boatapp.backend.service;

import com.boatapp.backend.dto.BoatRecord;
import com.boatapp.backend.dto.BoatRequest;
import com.boatapp.backend.entity.Boat;
import com.boatapp.backend.exception.BoatNotFoundException;
import com.boatapp.backend.mapper.BoatMapper;
import com.boatapp.backend.repository.BoatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Default implementation of {@link IBoatService}.
 */
@Slf4j
@Service
@Validated
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

    /**
     * {@inheritDoc}
     *
     * <p>Maps the request to a {@link Boat} entity via {@link BoatMapper},
     * persists it, and returns the saved entity as a {@link BoatRecord}.
     * The {@code createdAt} timestamp is set automatically by {@code @CreatedDate}.
     */
    @Override
    @Transactional
    public BoatRecord createBoat(BoatRequest request) {
        log.debug("createBoat called — name={}", request.name());
        Boat boat = boatMapper.toEntity(request);
        Boat saved = boatRepository.save(boat);
        log.debug("createBoat succeeded — id={}", saved.getId());
        return boatMapper.toRecord(saved);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Verifies the boat exists before deletion so that a meaningful
     * {@link BoatNotFoundException} is raised rather than a silent no-op.
     */
    @Override
    @Transactional
    public void deleteBoat(Long id) {
        log.debug("deleteBoat called — id={}", id);
        boatRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("deleteBoat — boat not found with id={}", id);
                    return new BoatNotFoundException(id);
                });
        boatRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Fetches the managed {@link Boat} entity, delegates field updates to
     * {@link BoatMapper#updateEntityFromRequest}, persists and returns
     * the result as a {@link BoatRecord}.
     */
    @Override
    @Transactional
    public BoatRecord updateBoat(Long id, BoatRequest request) {
        log.debug("updateBoat called — id={}, name={}", id, request.name());
        Boat boat = boatRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("updateBoat — boat not found with id={}", id);
                    return new BoatNotFoundException(id);
                });
        boatMapper.updateEntityFromRequest(request, boat);
        Boat saved = boatRepository.save(boat);
        log.debug("updateBoat succeeded — id={}", saved.getId());
        return boatMapper.toRecord(saved);
    }
}
