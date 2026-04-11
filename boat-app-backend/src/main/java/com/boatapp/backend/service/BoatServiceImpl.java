package com.boatapp.backend.service;

import com.boatapp.backend.mapper.BoatMapper;
import com.boatapp.backend.repository.BoatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link IBoatService}.
 */
@Service
@Transactional(readOnly = true)
public class BoatServiceImpl implements IBoatService {

    private final BoatRepository boatRepository;
    private final BoatMapper boatMapper;

    public BoatServiceImpl(BoatRepository boatRepository, BoatMapper boatMapper) {
        this.boatRepository = boatRepository;
        this.boatMapper = boatMapper;
    }

}

