package co.edu.escuelaing.techcup.communications.config;

import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless resource-server configuration. This microservice only validates tokens;
 * it exposes no authentication endpoint.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
                                                   SecurityErrorResponder errorResponder) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        // The STOMP CONNECT frame is authenticated by WsAuthChannelInterceptor.
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/reports/*/resolve")
                        .hasAnyRole(ParticipantRole.MODERATOR.name(), ParticipantRole.ORGANIZER.name())
                        .anyRequest().authenticated())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(errorResponder)
                        .accessDeniedHandler(errorResponder))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, errorResponder),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
