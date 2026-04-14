package com.boatapp.backend.security;

import com.boatapp.backend.controller.BoatControllerV1;
import com.boatapp.backend.controller.GlobalExceptionHandler;
import com.boatapp.backend.dto.BoatRecord;
import com.boatapp.backend.service.IBoatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security integration tests for the permission matrix defined in {@link Permission}.
 *
 * <p>Uses {@code @WebMvcTest} with the {@code jwt()} post-processor from
 * {@code spring-security-test}, which sets authorities directly in the security context
 * without contacting Keycloak. A {@link JwtDecoder} mock bean prevents auto-configuration
 * from attempting to fetch JWKS at startup.
 */
@WebMvcTest(BoatControllerV1.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@MockitoBean(types = JpaMetamodelMappingContext.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IBoatService boatService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("GET /api/v1/boats without token → 401 Unauthorized")
    void should_return401_when_noTokenProvided() throws Exception {
        mockMvc.perform(get("/api/v1/boats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/boats with ROLE_USER → 403 Forbidden")
    void should_return403_when_userTriesToCreateBoat() throws Exception {
        mockMvc.perform(post("/api/v1/boats")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test-Boat\",\"description\":\"A test boat\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/boats/1 with ROLE_USER → 403 Forbidden")
    void should_return403_when_userTriesToUpdateBoat() throws Exception {
        mockMvc.perform(put("/api/v1/boats/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test-Boat\",\"description\":\"A test boat\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/v1/boats/1 with ROLE_USER → 403 Forbidden")
    void should_return403_when_userTriesToDeleteBoat() throws Exception {
        mockMvc.perform(delete("/api/v1/boats/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/boats with ROLE_USER → 200 OK")
    void should_return200_when_userReadsBoatList() throws Exception {
        when(boatService.findAll(anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/boats")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/boats with ROLE_ADMIN → 201 Created")
    void should_return201_when_adminCreatesBoat() throws Exception {
        BoatRecord record = new BoatRecord(1L, "Test-Boat", "A test boat", Instant.now(), null);
        when(boatService.createBoat(any())).thenReturn(record);

        mockMvc.perform(post("/api/v1/boats")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test-Boat\",\"description\":\"A test boat\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PUT /api/v1/boats/1 with ROLE_ADMIN → 200 OK")
    void should_return200_when_adminUpdatesBoat() throws Exception {
        BoatRecord updated = new BoatRecord(1L, "Test-Boat", "A test boat", Instant.now(), Instant.now());
        when(boatService.updateBoat(any(Long.class), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/boats/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test-Boat\",\"description\":\"A test boat\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/boats/1 with ROLE_ADMIN → 204 No Content")
    void should_return204_when_adminDeletesBoat() throws Exception {
        doNothing().when(boatService).deleteBoat(1L);

        mockMvc.perform(delete("/api/v1/boats/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /actuator/health without token → not blocked by security (not 401/403)")
    void should_return200_when_actuatorHealthCalledWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError(
                            "Security must not block /actuator/health — expected NOT 401/403, got: " + status);
                    }
                });
    }
}


