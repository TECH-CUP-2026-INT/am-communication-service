package co.edu.escuelaing.techcup.communications.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @param secret shared HS256 secret; HMAC-SHA256 requires at least 256 bits.
 */
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(@NotBlank @Size(min = 32) String secret) {
}
