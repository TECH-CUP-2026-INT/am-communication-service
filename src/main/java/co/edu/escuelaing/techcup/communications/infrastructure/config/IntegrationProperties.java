package co.edu.escuelaing.techcup.communications.infrastructure.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Base URLs of the microservices this one talks to. */
@Validated
@ConfigurationProperties(prefix = "integrations")
public record IntegrationProperties(
        @NotNull @Valid Endpoint userService,
        @NotNull @Valid Endpoint teamService,
        @NotNull @Valid Endpoint notificationService
) {
    /**
     * @param existenceCheckEnabled whether {@code exists(id)} actually calls the downstream
     *                              service. Defaults to {@code true}; set to {@code false} while
     *                              a dependency (e.g. cc-teams-service, still a bare skeleton)
     *                              exposes no lookup endpoint yet, so callers aren't blocked.
     */
    public record Endpoint(@NotBlank String baseUrl, Boolean existenceCheckEnabled) {

        public Endpoint {
            existenceCheckEnabled = existenceCheckEnabled == null || existenceCheckEnabled;
        }
    }
}
