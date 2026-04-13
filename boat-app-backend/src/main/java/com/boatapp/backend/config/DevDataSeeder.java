package com.boatapp.backend.config;

import com.boatapp.backend.entity.Boat;
import com.boatapp.backend.repository.BoatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seeds the development database with 10 sample boats on every startup.
 *
 * <p>Using {@link ApplicationRunner} guarantees this runs <em>after</em> the full
 * Spring context — including JPA / Hibernate schema creation — is ready.
 * This avoids the initialization-ordering issues that come with {@code data.sql}
 * when using a non-embedded database like PostgreSQL.
 *
 * <p>With {@code ddl-auto: create-drop} the schema is recreated on every restart,
 * so the count check protects against accidental double-inserts if the profile is
 * somehow active on a persistent database.
 */
@Slf4j
@Component
@Profile(AppProfile.DEV_PROFILE)
@RequiredArgsConstructor
public class DevDataSeeder implements ApplicationRunner {

    private final BoatRepository boatRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        long count = boatRepository.count();
        if (count > 0) {
            log.info("Dev seed — database already has {} boat(s), skipping.", count);
            return;
        }

        List<Boat> boats = List.of(
                Boat.builder().name("Aurora")
                        .description("A graceful sailing yacht designed for long ocean passages.").build(),
                Boat.builder().name("Blue Horizon")
                        .description("A robust offshore cruiser built for heavy weather sailing.").build(),
                Boat.builder().name("Calypso")
                        .description("A classic wooden sloop, lovingly restored to original glory.").build(),
                Boat.builder().name("Drifter")
                        .description("A lightweight racing dinghy with exceptional upwind performance.").build(),
                Boat.builder().name("Eclipse")
                        .description("A modern catamaran offering stability and spacious living quarters.").build(),
                Boat.builder().name("Fair Wind")
                        .description("A traditional ketch rig ideal for single-handed ocean voyages.").build(),
                Boat.builder().name("Gale Runner")
                        .description("A high-performance offshore racer engineered for speed.").build(),
                Boat.builder().name("Harbour Light")
                        .description("A tender motorboat used for marina transfers and short trips.").build(),
                Boat.builder().name("Iron Maiden")
                        .description("A sturdy steel hull expedition vessel with ice-class rating.").build(),
                Boat.builder().name("Jade Pearl")
                        .description("A luxury motor yacht equipped with all modern amenities.").build()
        );

        boatRepository.saveAll(boats);
        log.info("Dev seed — inserted {} boats.", boats.size());
    }
}

