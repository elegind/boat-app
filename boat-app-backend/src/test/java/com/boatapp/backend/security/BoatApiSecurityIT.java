package com.boatapp.backend.security;

import com.boatapp.backend.repository.BoatRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack security integration test against a real PostgreSQL container.
 *
 * <p>Loads the complete Spring Boot context (web layer + service + repository + security)
 * and fires real HTTP requests through the entire security filter chain via {@link MockMvc}.
 *
 * <p>The {@link JwtDecoder} is mocked to avoid Keycloak connectivity at startup;
 * JWT authorities are injected via the {@code jwt()} post-processor from
 * {@code spring-security-test}, which populates the security context directly.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class BoatApiSecurityIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto",             () -> "create-drop");
        registry.add("spring.jpa.database-platform",              () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.sql.init.mode",                       () -> "never");
        registry.add("spring.jpa.defer-datasource-initialization", () -> "true");
    }

    /** Prevents SecurityConfig from fetching the Keycloak JWKS endpoint on startup. */
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoatRepository boatRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        boatRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/v1/boats without token → 401 Unauthorized")
    void should_return401_when_getCalledWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/boats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/boats without token → 401 Unauthorized")
    void should_return401_when_postCalledWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/boats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Test-Boat"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/v1/boats/1 without token → 401 Unauthorized")
    void should_return401_when_deleteCalledWithoutToken() throws Exception {
        mockMvc.perform(delete("/api/v1/boats/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/boats with ROLE_USER → 200 OK")
    void should_return200_when_userFetchesBoatList() throws Exception {
        mockMvc.perform(get("/api/v1/boats")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/boats with ROLE_USER → response contains pagination metadata")
    void should_returnPaginationMetadata_when_userFetchesBoatList() throws Exception {
        mockMvc.perform(get("/api/v1/boats")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/boats with ROLE_USER → 403 Forbidden")
    void should_return403_when_userTriesToCreateBoat() throws Exception {
        mockMvc.perform(post("/api/v1/boats")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Test-Boat"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/boats/1 with ROLE_USER → 403 Forbidden")
    void should_return403_when_userTriesToUpdateBoat() throws Exception {
        mockMvc.perform(put("/api/v1/boats/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Test-Boat"))))
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
    @DisplayName("POST /api/v1/boats with ROLE_ADMIN → 201 Created with body")
    void should_return201WithBody_when_adminCreatesBoat() throws Exception {
        mockMvc.perform(post("/api/v1/boats")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "Fair-Wind", "description", "A classic ketch"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Fair-Wind"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("PUT /api/v1/boats/{id} with ROLE_ADMIN → 200 OK when boat exists")
    void should_return200_when_adminUpdatesExistingBoat() throws Exception {
        // ARRANGE — create via POST so the real id is used
        String response = mockMvc.perform(post("/api/v1/boats")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "To-Update", "description", "Before"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        // ACT + ASSERT
        mockMvc.perform(put("/api/v1/boats/" + id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "Updated", "description", "After"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.description").value("After"));
    }

    @Test
    @DisplayName("DELETE /api/v1/boats/{id} with ROLE_ADMIN → 204 No Content when boat exists")
    void should_return204_when_adminDeletesExistingBoat() throws Exception {
        // ARRANGE
        String response = mockMvc.perform(post("/api/v1/boats")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "To-Delete", "description", "Gone soon"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        // ACT + ASSERT
        mockMvc.perform(delete("/api/v1/boats/" + id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());

        // Verify the boat is actually gone from the database
        assertThat(boatRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("PUT /api/v1/boats/{id} with ROLE_ADMIN → 404 Not Found when boat does not exist")
    void should_return404_when_adminUpdatesNonExistingBoat() throws Exception {
        mockMvc.perform(put("/api/v1/boats/" + Long.MAX_VALUE)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "Ghost", "description", "Not here"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/boats/{id} with ROLE_ADMIN → 404 Not Found when boat does not exist")
    void should_return404_when_adminDeletesNonExistingBoat() throws Exception {
        mockMvc.perform(delete("/api/v1/boats/" + Long.MAX_VALUE)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/boats with ROLE_ADMIN → 400 Bad Request when name is blank")
    void should_return400_when_adminCreatesBoatWithBlankName() throws Exception {
        mockMvc.perform(post("/api/v1/boats")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /actuator/health without token → 200 OK (always public)")
    void should_return200_when_actuatorHealthCalledWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /v3/api-docs without token → 200 OK (public in dev profile)")
    void should_return200_when_swaggerApiDocsCalledWithoutToken() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /swagger-ui/index.html without token → 200 OK (public in dev profile)")
    void should_return200_when_swaggerUiCalledWithoutToken() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}




