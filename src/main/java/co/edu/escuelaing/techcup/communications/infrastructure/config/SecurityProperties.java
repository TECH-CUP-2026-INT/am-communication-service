package co.edu.escuelaing.techcup.communications.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Paths that skip authentication entirely: gateway schema discovery, health probes, and
 * similar cross-cutting integration needs. Kept configurable so a future request like "open
 * this path for the API gateway" is an environment variable change, not a code change and
 * redeploy.
 */
@Validated
@ConfigurationProperties(prefix = "security")
public record SecurityProperties(List<String> publicPaths) {

    public SecurityProperties {
        publicPaths = (publicPaths == null || publicPaths.isEmpty())
                ? List.of("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                        "/actuator/health", "/actuator/info", "/actuator/prometheus")
                : List.copyOf(publicPaths);
    }
}
