package co.edu.escuelaing.techcup.communications.infrastructure.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The WebSocket handshake and the REST CORS filter both read this list; it must never
 * silently fall back to a wildcard when the operator forgets to set it.
 */
class WebSocketPropertiesTest {

    @Test
    void defaultsToLocalhostWhenNotConfigured() {
        WebSocketProperties properties = new WebSocketProperties(null);

        assertThat(properties.allowedOrigins()).containsExactly("http://localhost:*");
    }

    @Test
    void defaultsToLocalhostWhenConfiguredEmpty() {
        WebSocketProperties properties = new WebSocketProperties(List.of());

        assertThat(properties.allowedOrigins()).containsExactly("http://localhost:*");
    }

    @Test
    void keepsTheConfiguredOriginsWhenPresent() {
        WebSocketProperties properties = new WebSocketProperties(List.of("https://app.example.com"));

        assertThat(properties.allowedOrigins()).containsExactly("https://app.example.com");
    }
}
