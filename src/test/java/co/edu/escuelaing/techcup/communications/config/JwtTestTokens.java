package co.edu.escuelaing.techcup.communications.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

/** Mints the HS256 tokens an external authentication service would issue. */
final class JwtTestTokens {

    static final String SECRET = "test-secret-key-for-hs256-signing-must-be-long-enough-256-bits";

    private JwtTestTokens() {
    }

    static String signedWith(String secret, UUID userId, String username, Collection<String> roles, Duration validFor) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId == null ? "not-a-uuid" : userId.toString())
                .claim("username", username)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(validFor)))
                .signWith(key(secret))
                .compact();
    }

    static String valid(UUID userId, String username, Collection<String> roles) {
        return signedWith(SECRET, userId, username, roles, Duration.ofMinutes(5));
    }

    static String expired(UUID userId) {
        return signedWith(SECRET, userId, "expired", java.util.Set.of(), Duration.ofMinutes(-5));
    }

    static String bearer(String token) {
        return "Bearer " + token;
    }

    private static SecretKey key(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
