package co.edu.escuelaing.techcup.communications.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @param secret shared HS256 secret; HMAC-SHA256 requires at least 256 bits.
 * @param issuer expected {@code iss} claim; blank means issuer is not checked.
 * @param audience expected {@code aud} claim; blank means audience is not checked.
 * @param clockSkewSeconds leeway applied to expiration/not-before checks.
 */
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        @NotBlank @Size(min = 32) String secret,
        String issuer,
        String audience,
        @PositiveOrZero Integer clockSkewSeconds) {

    public JwtProperties {
        issuer = blankToNull(issuer);
        audience = blankToNull(audience);
        clockSkewSeconds = clockSkewSeconds == null ? 0 : clockSkewSeconds;
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
