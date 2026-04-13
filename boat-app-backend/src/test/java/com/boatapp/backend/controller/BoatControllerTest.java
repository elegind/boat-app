package com.boatapp.backend.controller;

import com.boatapp.backend.dto.BoatRecord;
import com.boatapp.backend.security.SecurityConfig;
import com.boatapp.backend.service.IBoatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice test for {@link BoatControllerV1}.
 */
@WebMvcTest(BoatControllerV1.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@MockitoBean(types = JpaMetamodelMappingContext.class)
class BoatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IBoatService boatService;


    // ── GET /api/v1/boats ─────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/boats → 200 with page containing 2 boats (default params)")
    void getAllBoats_should_returnPageOfBoats_when_defaultParams() throws Exception {
        // ARRANGE
        List<BoatRecord> boats = List.of(
                new BoatRecord(1L, "Aurora", "A sailing yacht", Instant.now()),
                new BoatRecord(2L, "Blue Horizon", "An offshore cruiser", Instant.now())
        );
        when(boatService.findAll(0, 5))
                .thenReturn(new PageImpl<>(boats, PageRequest.of(0, 5), 2));

        // ACT and ASSERT
        mockMvc.perform(get("/api/v1/boats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Aurora"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/boats?page=0&size=5 → 200 with 5 boats")
    void getAllBoats_should_returnFiveBoats_when_sizeIsFive() throws Exception {
        // ARRANGE
        when(boatService.findAll(0, 5))
                .thenReturn(new PageImpl<>(boats(1, 2, 3, 4, 5), PageRequest.of(0, 5), 10));

        // ACT and ASSERT
        mockMvc.perform(get("/api/v1/boats").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)));
    }

    @Test
    @DisplayName("GET /api/v1/boats?page=1&size=5 → 200 with page number 1")
    void getAllBoats_should_returnSecondPage_when_pageIsOne() throws Exception {
        // ARRANGE
        when(boatService.findAll(1, 5))
                .thenReturn(new PageImpl<>(boats(6, 7, 8), PageRequest.of(1, 5), 8));

        // ACT and ASSERT
        mockMvc.perform(get("/api/v1/boats").param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/boats → 200 with empty page when no boats exist")
    void getAllBoats_should_returnEmptyPage_when_noBoatsExist() throws Exception {
        // ARRANGE
        when(boatService.findAll(0, 5))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 5), 0));

        // ACT and ASSERT
        mockMvc.perform(get("/api/v1/boats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/v1/boats → 500 with error envelope when service throws a DataAccessException")
    void getAllBoats_should_return500_when_serviceThrowsDataAccessException() throws Exception {
        // ARRANGE
        when(boatService.findAll(0, 5))
                .thenThrow(new DataAccessResourceFailureException("Connection to database lost"));

        // ACT and ASSERT
        mockMvc.perform(get("/api/v1/boats"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("GET /api/v1/boats?page=-1 → 400 with constraint violation message")
    void getAllBoats_should_return400_when_pageIsNegative() throws Exception {
        mockMvc.perform(get("/api/v1/boats").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(containsString("page must be >= 0")));
    }

    @Test
    @DisplayName("GET /api/v1/boats?size=0 → 400 with constraint violation message")
    void getAllBoats_should_return400_when_sizeIsZero() throws Exception {
        mockMvc.perform(get("/api/v1/boats").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(containsString("size must be >= 1")));
    }

    @Test
    @DisplayName("GET /api/v1/boats?size=-5 → 400 with constraint violation message")
    void getAllBoats_should_return400_when_sizeIsNegative() throws Exception {
        mockMvc.perform(get("/api/v1/boats").param("size", "-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(containsString("size must be >= 1")));
    }

    @Test
    @DisplayName("GET /api/v1/boats?page=-1&size=0 → 400 and both violations present in message")
    void getAllBoats_should_return400_when_bothParamsAreInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/boats").param("page", "-1").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(containsString("page must be >= 0")))
                .andExpect(jsonPath("$.message").value(containsString("size must be >= 1")));
    }

    @Test
    @DisplayName("GET /api/v1/boats?page=abc → 400 when page is not a number")
    void getAllBoats_should_return400_when_pageIsNotNumeric() throws Exception {
        mockMvc.perform(get("/api/v1/boats").param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(containsString("'page'")))
                .andExpect(jsonPath("$.message").value(containsString("'abc'")));
    }

    /**
     * Builds a list of {@link BoatRecord} from a varargs of IDs.
     * Each record gets name "Boat {id}" and no description.
     */
    private List<BoatRecord> boats(int... ids) {
        return Arrays.stream(ids)
                .mapToObj(id -> new BoatRecord((long) id, "Boat " + id, null, Instant.now()))
                .toList();
    }
}






