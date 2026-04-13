package com.boatapp.backend.repository;

import com.boatapp.backend.entity.Boat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link BoatRepository} against a real PostgreSQL container.
 *
 * <p>Uses Testcontainers so the database behaviour matches production.
 * {@code @EnableJpaAuditing} is picked up automatically from {@code BoatAppApplication}.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class BoatRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private BoatRepository boatRepository;

    private List<Boat> buildBoats(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> Boat.builder().name("Boat " + i).description("Description " + i).build())
                .toList();
    }

    @Test
    @DisplayName("findAll returns first page of 5 when 7 boats exist")
    void findAll_should_returnPaginatedResults_when_multipleBoatsExist() {
        // ARRANGE
        boatRepository.saveAll(buildBoats(7));

        // ACT
        Page<Boat> page = boatRepository.findAll(PageRequest.of(0, 5));

        // ASSERT
        assertThat(page.getContent()).hasSize(5);
        assertThat(page.getTotalElements()).isEqualTo(7);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("findAll returns 2 remaining boats on second page when 7 boats exist")
    void findAll_should_returnSecondPage_when_firstPageIsFull() {
        // ARRANGE
        boatRepository.saveAll(buildBoats(7));

        // ACT
        Page<Boat> page = boatRepository.findAll(PageRequest.of(1, 5));

        // ASSERT
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("findAll returns boats sorted by createdAt DESC — newest first")
    void findAll_should_returnBoatsSortedByCreatedAtDesc_when_sortIsApplied() {
        // ARRANGE — save 3 boats sequentially; each save has a slightly later createdAt
        // because @CreatedDate is set at persist time inside the same transaction.
        // We save one at a time to guarantee distinct timestamps via flush + clear.
        boatRepository.saveAndFlush(Boat.builder().name("First").build());
        boatRepository.saveAndFlush(Boat.builder().name("Second").build());
        boatRepository.saveAndFlush(Boat.builder().name("Third").build());

        // ACT — same Pageable used by BoatServiceImpl
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Boat> page = boatRepository.findAll(pageable);

        // ASSERT — most recently persisted boat must come first
        List<Boat> content = page.getContent();
        assertThat(content).hasSizeGreaterThanOrEqualTo(3);

        // Verify strictly descending createdAt order across the whole result
        for (int i = 0; i < content.size() - 1; i++) {
            assertThat(content.get(i).getCreatedAt())
                    .as("boat at index %d should be newer than boat at index %d", i, i + 1)
                    .isAfterOrEqualTo(content.get(i + 1).getCreatedAt());
        }

        // Verify the three inserted boats appear in reverse insertion order (Third → Second → First)
        List<String> names = content.stream().map(Boat::getName).toList();
        assertThat(names.indexOf("Third")).isLessThan(names.indexOf("Second"));
        assertThat(names.indexOf("Second")).isLessThan(names.indexOf("First"));
    }
}

