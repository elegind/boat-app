package com.boatapp.backend.repository;

import com.boatapp.backend.entity.Boat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Boat} entities.
 */
@Repository
public interface BoatRepository extends JpaRepository<Boat, Long> {
}

