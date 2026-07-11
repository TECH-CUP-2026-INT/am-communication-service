package co.edu.escuelaing.techcup.communications.config;

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
        @NotNull @Valid Endpoint notificationService,
        @NotNull @Valid Endpoint auditService
) {
    public record Endpoint(@NotBlank String baseUrl) {
    }
}
