package co.edu.escuelaing.techcup.communications.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Origins allowed to open the STOMP handshake and to call the REST API from a browser.
 * Defaults to localhost so a fresh checkout still works without extra configuration.
 */
@Validated
@ConfigurationProperties(prefix = "websocket")
public record WebSocketProperties(List<String> allowedOrigins) {

    public WebSocketProperties {
        allowedOrigins = (allowedOrigins == null || allowedOrigins.isEmpty())
                ? List.of("http://localhost:*")
                : List.copyOf(allowedOrigins);
    }
}
