package com.boatapp.backend.security;

import com.boatapp.backend.config.AppProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Security configuration for the Boat App API.
 *
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    /**
     * URL used to FETCH the Keycloak public keys (JWKS).
     */
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    /**
     * Expected value of the {@code iss} claim inside every JWT.
     */
    @Value("${keycloak.issuer-uri}")
    private String keycloakIssuerUri;

    /**
     * Configures the main security filter chain.
     *
     * @param http the Spring Security HTTP builder
     * @return the built {@link SecurityFilterChain}
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(PublicEndpoints.ALWAYS).permitAll();
                    if (!activeProfile.contains(AppProfile.PROD.getValue())) {
                        auth.requestMatchers(PublicEndpoints.NON_PROD).permitAll();
                    }
                    for (Permission p : Permission.values()) {
                        auth.requestMatchers(p.method, p.pattern).hasAuthority(p.role);
                    }
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    /**
     * Extracts Keycloak realm roles from the {@code realm_access.roles} JWT claim
     *
     * @return a configured {@link JwtAuthenticationConverter}
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractRealmRoles);
        return converter;
    }

    /**
     * Reads {@code realm_access.roles} from the JWT and converts each entry to a
     * {@link SimpleGrantedAuthority}. Returns an empty collection if the claim is absent.
     *
     * @param jwt the decoded JWT
     * @return a collection of granted authorities representing the realm roles
     */
    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return Collections.emptyList();
        }
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.get("roles");
        if (roles == null) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
    }

    /**
     * Custom {@link JwtDecoder} that solves the Docker networking split:
     * <ul>
     *   <li>Fetches the JWKS (public keys) from the <em>internal</em> Docker URL
     *       ({@code http://auth-mock:9000/...}) so the backend container can reach
     *       Keycloak over the Docker bridge network.</li>
     *   <li>Validates the {@code iss} claim against the <em>external</em> URL
     *       ({@code http://localhost:9000/...}) because that is what Keycloak puts
     *       in the token — the browser obtained it from localhost, not auth-mock.</li>
     * </ul>
     * Without this split, Spring would reject every token with 401 because the
     * {@code iss} claim ({@code localhost:9000}) would not match the configured
     * issuer ({@code auth-mock:9000}).
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        OAuth2TokenValidator<Jwt> validator = JwtValidators.createDefaultWithIssuer(keycloakIssuerUri);
        decoder.setJwtValidator(validator);
        return decoder;
    }

    /**
     * <ul>
     *   <li>Allows {@code ng serve} on localhost:4200 (local dev)</li>
     *   <li>Allows nginx-proxied requests on localhost:80 / localhost:4200 (Docker)</li>
     * </ul>
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:[*]",
                "http://127.0.0.1:[*]"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
