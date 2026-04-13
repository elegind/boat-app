package com.boatapp.backend.service;

import com.boatapp.backend.dto.BoatRecord;
import com.boatapp.backend.entity.Boat;
import com.boatapp.backend.exception.BoatNotFoundException;
import com.boatapp.backend.mapper.BoatMapper;
import com.boatapp.backend.repository.BoatRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link BoatServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class BoatServiceTest {

    @Mock
    private BoatRepository boatRepository;

    @Mock
    private BoatMapper boatMapper;

    @InjectMocks
    private BoatServiceImpl boatService;

    @Test
    @DisplayName("findAll returns a Page<BoatRecord> when boats exist")
    void findAll_should_returnPageOfBoatRecords_when_boatsExist() {
        // ARRANGE
        Boat boat1 = Boat.builder().id(1L).name("Aurora").description("Desc 1").build();
        Boat boat2 = Boat.builder().id(2L).name("Blue Horizon").description("Desc 2").build();
        BoatRecord record1 = new BoatRecord(1L, "Aurora", "Desc 1", Instant.now());
        BoatRecord record2 = new BoatRecord(2L, "Blue Horizon", "Desc 2", Instant.now());

        Page<Boat> boatPage = new PageImpl<>(List.of(boat1, boat2), PageRequest.of(0, 5), 2);
        when(boatRepository.findAll(any(Pageable.class))).thenReturn(boatPage);
        when(boatMapper.toRecord(boat1)).thenReturn(record1);
        when(boatMapper.toRecord(boat2)).thenReturn(record2);

        // ACT
        Page<BoatRecord> result = boatService.findAll(0, 5);

        // ASSERT
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().getFirst().name()).isEqualTo("Aurora");
    }

    @Test
    @DisplayName("findAll calls repository with the correct Pageable when params are provided")
    void findAll_should_callRepositoryWithCorrectPageable_when_paramsProvided() {
        // ARRANGE
        Page<Boat> emptyPage = new PageImpl<>(List.of(), PageRequest.of(2, 10), 0);
        when(boatRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // ACT
        boatService.findAll(2, 10);

        // ASSERT
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(boatRepository).findAll(pageableCaptor.capture());

        Pageable captured = pageableCaptor.getValue();
        assertThat(captured.getPageNumber()).isEqualTo(2);
        assertThat(captured.getPageSize()).isEqualTo(10);
        assertThat(captured.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(captured.getSort().getOrderFor("createdAt").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("findAll returns an empty page when no boats exist")
    void findAll_should_returnEmptyPage_when_noBoatsExist() {
        // ARRANGE
        Page<Boat> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 5), 0);
        when(boatRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // ACT
        Page<BoatRecord> result = boatService.findAll(0, 5);

        // ASSERT
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
        assertThat(result.getNumber()).isZero();
    }

    @Test
    @DisplayName("findAll returns correct pagination metadata when total is spread across two pages")
    void findAll_should_returnCorrectPaginationMetadata_when_totalIsSpreadAcrossTwoPages() {
        // ARRANGE
        List<Boat> fiveBoats = IntStream.rangeClosed(1, 5)
                .mapToObj(i -> Boat.builder().id((long) i).name("Boat " + i).build())
                .toList();

        Page<Boat> firstPage = new PageImpl<>(fiveBoats, PageRequest.of(0, 5), 10);
        when(boatRepository.findAll(argThat((Pageable p) -> p.getPageNumber() == 0))).thenReturn(firstPage);
        fiveBoats.forEach(boat ->
                when(boatMapper.toRecord(boat))
                        .thenReturn(new BoatRecord(boat.getId(), boat.getName(), null, Instant.now()))
        );

        // ACT
        Page<BoatRecord> result = boatService.findAll(0, 5);

        // ASSERT
        assertThat(result.getTotalElements()).isEqualTo(10);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getNumber()).isZero();          // first page
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.hasPrevious()).isFalse();
    }

    @Test
    @DisplayName("findAll returns the second page when page is one")
    void findAll_should_returnSecondPage_when_pageIsOne() {
        // ARRANGE
        List<Boat> twoBoats = IntStream.rangeClosed(6, 7)
                .mapToObj(i -> Boat.builder().id((long) i).name("Boat " + i).build())
                .toList();

        Page<Boat> secondPage = new PageImpl<>(twoBoats, PageRequest.of(1, 5), 7);
        when(boatRepository.findAll(argThat((Pageable p) -> p.getPageNumber() == 1))).thenReturn(secondPage);
        twoBoats.forEach(boat ->
                when(boatMapper.toRecord(boat))
                        .thenReturn(new BoatRecord(boat.getId(), boat.getName(), null, Instant.now()))
        );

        // ACT
        Page<BoatRecord> result = boatService.findAll(1, 5);

        // ASSERT
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(7);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.isLast()).isTrue();
        assertThat(result.hasPrevious()).isTrue();
    }

    @Test
    @DisplayName("findAll propagates a DataAccessException when the repository throws one")
    void findAll_should_propagateException_when_repositoryThrowsDataAccessException() {
        // ARRANGE
        when(boatRepository.findAll(any(Pageable.class)))
                .thenThrow(new DataAccessResourceFailureException("Connection to database lost"));

        // ACT + ASSERT
        assertThatThrownBy(() -> boatService.findAll(0, 5))
                .isInstanceOf(DataAccessResourceFailureException.class)
                .hasMessageContaining("Connection to database lost");
    }

    @Test
    @DisplayName("deleteBoat deletes the boat when the id exists")
    void deleteBoat_should_deleteBoat_when_idExists() {
        // ARRANGE
        Boat boat = Boat.builder().id(1L).name("Aurora").build();
        when(boatRepository.findById(1L)).thenReturn(Optional.of(boat));

        // ACT
        boatService.deleteBoat(1L);

        // ASSERT
        verify(boatRepository).findById(1L);
        verify(boatRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteBoat throws BoatNotFoundException when the id does not exist")
    void deleteBoat_should_throwBoatNotFoundException_when_idNotFound() {
        // ARRANGE
        when(boatRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> boatService.deleteBoat(99L))
                .isInstanceOf(BoatNotFoundException.class)
                .hasMessageContaining("99");

        verify(boatRepository, never()).deleteById(any());
    }
}

