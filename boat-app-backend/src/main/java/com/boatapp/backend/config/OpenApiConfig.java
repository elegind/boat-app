package com.boatapp.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc / OpenAPI 3 configuration.
 * Using a multi version strategy
 */
@Configuration
public class OpenApiConfig {

    /**
     * Provides common contact / license info reused by every group customizer.
     */
    @Bean
    public OpenAPI globalOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Boat App API")
                        .description("REST API for managing a fleet of boats.")
                        .contact(new Contact()
                                .name("Boat App Team")));
    }

    /**
     * <b>v1</b> group — routes under {@code /api/v1/**}.
     */
    @Bean
    public GroupedOpenApi v1Group() {
        return GroupedOpenApi.builder()
                .group("v1")
                .displayName("Boat API — v1")
                .pathsToMatch("/api/v1/**")
                .addOpenApiCustomizer(openApi ->
                        openApi.getInfo()
                                .title("Boat App API")
                                .version("v1"))
                .build();
    }
}
