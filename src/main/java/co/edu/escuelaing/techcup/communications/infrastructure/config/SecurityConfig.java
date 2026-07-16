package co.edu.escuelaing.techcup.communications.infrastructure.config;

import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Stateless resource-server configuration. This microservice only validates tokens;
 * it exposes no authentication endpoint.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * cc-identity-service has no {@code MODERATOR} role of its own; its administrative role is
     * {@code ADMIN}. Accepting it here lets identity-issued tokens reach moderation endpoints
     * without requiring a role this service invented.
     */
    static final String IDENTITY_ADMIN_ROLE = "ADMIN";

    // CSRF is intentionally disabled: this is a stateless resource server authenticated via the
    // Authorization: Bearer header (no session cookies). CSRF attacks rely on the browser attaching
    // ambient credentials automatically, which cannot happen for a bearer-token API, so the
    // protection adds no value here. See Spring Security docs on CSRF and stateless APIs.
    @SuppressWarnings("java:S4502")
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
                                                   SecurityErrorResponder errorResponder,
                                                   SecurityProperties securityProperties,
                                                   CorsConfigurationSource corsConfigurationSource) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers(securityProperties.publicPaths().toArray(String[]::new)).permitAll()
                        .requestMatchers(HttpMethod.POST, "/reports/*/resolve")
                        .hasAnyRole(ParticipantRole.MODERATOR.name(), ParticipantRole.ORGANIZER.name(),
                                IDENTITY_ADMIN_ROLE)
                        .requestMatchers("/faqs", "/faqs/*")
                        .hasAnyRole(ParticipantRole.MODERATOR.name(), ParticipantRole.ORGANIZER.name(),
                                IDENTITY_ADMIN_ROLE)
                        .anyRequest().authenticated())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(errorResponder)
                        .accessDeniedHandler(errorResponder))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, errorResponder),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(WebSocketProperties webSocketProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(webSocketProperties.allowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
