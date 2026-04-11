package com.boatapp.backend.controller;

import com.boatapp.backend.security.SecurityConfig;
import com.boatapp.backend.service.IBoatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test for {@link BoatControllerV1}.
 *
 */
@WebMvcTest(BoatControllerV1.class)
@Import(SecurityConfig.class)
@MockitoBean(types = JpaMetamodelMappingContext.class)
class BoatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IBoatService boatService;

    @Test
    @DisplayName("GET /api/v1/boats/hello → 200 with greeting text")
    void helloEndpoint_shouldReturns200() throws Exception {
        mockMvc.perform(get("/api/v1/boats/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello from BoatController"));
    }
}






