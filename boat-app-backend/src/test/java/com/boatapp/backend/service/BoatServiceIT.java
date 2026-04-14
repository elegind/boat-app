package com.boatapp.backend.service;

import com.boatapp.backend.dto.BoatRecord;
import com.boatapp.backend.dto.BoatRequest;
import com.boatapp.backend.exception.BoatNotFoundException;
import com.boatapp.backend.repository.BoatRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test for {@link BoatServiceImpl} wired to a real PostgreSQL database.
 *
 * <p>Loads the full Spring application context (excluding Keycloak — {@link JwtDecoder}
 * is mocked so no JWKS endpoint is contacted at startup).
 *
 * <p>Each test method runs inside a transaction that is rolled back after it returns,
 * leaving the database clean for the next test.
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class BoatServiceIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Create schema from scratch on each run; disable seed data.sql
        registry.add("spring.jpa.hibernate.ddl-auto",              () -> "create-drop");
        registry.add("spring.jpa.database-platform",               () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.sql.init.mode",                        () -> "never");
        registry.add("spring.jpa.defer-datasource-initialization",  () -> "true");
    }

    /** Prevents {@link org.springframework.security.config.annotation.web.configuration.EnableWebSecurity}
     *  from fetching the Keycloak JWKS endpoint on startup. */
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private IBoatService boatService;

    @Autowired
    private BoatRepository boatRepository;

    @Test
    @DisplayName("createBoat persists a boat with a generated id when request is valid")
    void createBoat_should_persistBoatWithGeneratedId_when_requestIsValid() {
        // ACT
        BoatRecord result = boatService.createBoat(new BoatRequest("Aurora", "A graceful sailing yacht"));

        // ASSERT
        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("Aurora");
        assertThat(result.description()).isEqualTo("A graceful sailing yacht");
        assertThat(boatRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("createBoat sets createdAt and updatedAt automatically when boat is first persisted")
    void createBoat_should_setAuditTimestamps_when_boatIsPersisted() {
        // ACT
        BoatRecord result = boatService.createBoat(new BoatRequest("Blue Horizon", null));

        // ASSERT
        assertThat(result.createdAt()).isNotNull();
        assertThat(result.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("createBoat persists a boat with null description when description is omitted")
    void createBoat_should_persistNullDescription_when_descriptionIsOmitted() {
        // ACT
        BoatRecord result = boatService.createBoat(new BoatRequest("Calypso", null));

        // ASSERT
        assertThat(result.description()).isNull();
        assertThat(boatRepository.findById(result.id()))
                .isPresent()
                .hasValueSatisfying(b -> assertThat(b.getDescription()).isNull());
    }

    @Test
    @DisplayName("findAll returns an empty page when no boats exist in the database")
    void findAll_should_returnEmptyPage_when_noBoatsExistInDatabase() {
        // ACT
        Page<BoatRecord> page = boatService.findAll(0, 5);

        // ASSERT
        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findAll returns all created boats when boats exist in the database")
    void findAll_should_returnAllCreatedBoats_when_boatsExistInDatabase() {
        // ARRANGE
        boatService.createBoat(new BoatRequest("Calypso", "Classic sloop"));
        boatService.createBoat(new BoatRequest("Drifter", "Racing dinghy"));
        boatService.createBoat(new BoatRequest("Eclipse", "Modern catamaran"));

        // ACT
        Page<BoatRecord> page = boatService.findAll(0, 10);

        // ASSERT
        assertThat(page.getTotalElements()).isEqualTo(3);
        List<String> names = page.getContent().stream().map(BoatRecord::name).toList();
        assertThat(names).containsExactlyInAnyOrder("Calypso", "Drifter", "Eclipse");
    }

    @Test
    @DisplayName("findAll returns boats sorted by createdAt DESC when multiple boats exist")
    void findAll_should_returnBoatsSortedByCreatedAtDesc_when_multipleBoatsExist() throws InterruptedException {
        // ARRANGE — small sleep between saves to guarantee distinct createdAt timestamps
        boatService.createBoat(new BoatRequest("First", null));
        Thread.sleep(20);
        boatService.createBoat(new BoatRequest("Second", null));
        Thread.sleep(20);
        boatService.createBoat(new BoatRequest("Third", null));

        // ACT
        Page<BoatRecord> page = boatService.findAll(0, 10);

        // ASSERT — newest created first
        List<String> names = page.getContent().stream().map(BoatRecord::name).toList();
        assertThat(names.indexOf("Third")).isLessThan(names.indexOf("Second"));
        assertThat(names.indexOf("Second")).isLessThan(names.indexOf("First"));
    }

    @Test
    @DisplayName("findAll returns correct page metadata when results span multiple pages")
    void findAll_should_returnCorrectPageMetadata_when_resultsSpanMultiplePages() {
        // ARRANGE
        IntStream.rangeClosed(1, 7)
                .forEach(i -> boatService.createBoat(new BoatRequest("Boat-" + i, null)));

        // ACT
        Page<BoatRecord> firstPage  = boatService.findAll(0, 5);
        Page<BoatRecord> secondPage = boatService.findAll(1, 5);

        // ASSERT
        assertThat(firstPage.getTotalElements()).isEqualTo(7);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.getContent()).hasSize(5);
        assertThat(secondPage.getContent()).hasSize(2);
        assertThat(secondPage.isLast()).isTrue();
    }

    @Test
    @DisplayName("updateBoat persists updated name and description when boat exists")
    void updateBoat_should_persistUpdatedFields_when_boatExists() {
        // ARRANGE
        BoatRecord created = boatService.createBoat(new BoatRequest("Old-Name", "Old desc"));

        // ACT
        BoatRecord updated = boatService.updateBoat(created.id(), new BoatRequest("New-Name", "New desc"));

        // ASSERT
        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.name()).isEqualTo("New-Name");
        assertThat(updated.description()).isEqualTo("New desc");
        // createdAt must never change on update
        assertThat(updated.createdAt()).isEqualTo(created.createdAt());
    }

    @Test
    @DisplayName("updateBoat reflects the change in the database when boat exists")
    void updateBoat_should_reflectChangeInDatabase_when_boatExists() {
        // ARRANGE
        BoatRecord created = boatService.createBoat(new BoatRequest("Fair Wind", "Old desc"));

        // ACT
        boatService.updateBoat(created.id(), new BoatRequest("Fair-Wind-V2", "New desc"));

        // ASSERT — reload from repository to verify the DB state
        assertThat(boatRepository.findById(created.id()))
                .isPresent()
                .hasValueSatisfying(boat -> {
                    assertThat(boat.getName()).isEqualTo("Fair-Wind-V2");
                    assertThat(boat.getDescription()).isEqualTo("New desc");
                });
    }

    @Test
    @DisplayName("updateBoat throws BoatNotFoundException when id does not exist")
    void updateBoat_should_throwBoatNotFoundException_when_idNotFound() {
        // ACT + ASSERT
        assertThatThrownBy(() -> boatService.updateBoat(Long.MAX_VALUE, new BoatRequest("X", null)))
                .isInstanceOf(BoatNotFoundException.class)
                .hasMessageContaining(String.valueOf(Long.MAX_VALUE));
    }

    @Test
    @DisplayName("deleteBoat removes the boat from the database when id exists")
    void deleteBoat_should_removeBoatFromDatabase_when_idExists() {
        // ARRANGE
        BoatRecord created = boatService.createBoat(new BoatRequest("To-Delete", "Will be gone"));
        assertThat(boatRepository.count()).isEqualTo(1);

        // ACT
        boatService.deleteBoat(created.id());

        // ASSERT
        assertThat(boatRepository.findById(created.id())).isEmpty();
        assertThat(boatRepository.count()).isZero();
    }

    @Test
    @DisplayName("deleteBoat throws BoatNotFoundException when id does not exist")
    void deleteBoat_should_throwBoatNotFoundException_when_idNotFound() {
        // ACT + ASSERT
        assertThatThrownBy(() -> boatService.deleteBoat(Long.MAX_VALUE))
                .isInstanceOf(BoatNotFoundException.class)
                .hasMessageContaining(String.valueOf(Long.MAX_VALUE));
    }

    @Test
    @DisplayName("deleteBoat does not remove other boats when one boat is deleted")
    void deleteBoat_should_notAffectOtherBoats_when_oneBoatDeleted() {
        // ARRANGE
        BoatRecord keep   = boatService.createBoat(new BoatRequest("Keeper", null));
        BoatRecord remove = boatService.createBoat(new BoatRequest("Goner",  null));

        // ACT
        boatService.deleteBoat(remove.id());

        // ASSERT
        assertThat(boatRepository.count()).isEqualTo(1);
        assertThat(boatRepository.findById(keep.id())).isPresent();
        assertThat(boatRepository.findById(remove.id())).isEmpty();
    }
}

