package co.edu.escuelaing.techcup.communications.infrastructure.config;

import co.edu.escuelaing.techcup.communications.domain.exception.InvalidTokenException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(new JwtProperties(JwtTestTokens.SECRET, null, null, 0));

    private final UUID userId = UUID.randomUUID();

    private String tokenWith(String issuer, String audience) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", "alice")
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofMinutes(5))))
                .signWith(Keys.hmacShaKeyFor(JwtTestTokens.SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    @Test
    void acceptsATokenMatchingTheConfiguredIssuerAndAudience() {
        JwtService strict = new JwtService(new JwtProperties(JwtTestTokens.SECRET, "auth-service", "communications", 0));

        AuthenticatedUser user = strict.parse(tokenWith("auth-service", "communications"));

        assertThat(user.userId()).isEqualTo(userId);
    }

    @Test
    void rejectsATokenWithTheWrongIssuer() {
        JwtService strict = new JwtService(new JwtProperties(JwtTestTokens.SECRET, "auth-service", null, 0));

        assertThatThrownBy(() -> strict.parse(tokenWith("someone-else", "communications")))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void rejectsATokenWithTheWrongAudience() {
        JwtService strict = new JwtService(new JwtProperties(JwtTestTokens.SECRET, null, "communications", 0));

        assertThatThrownBy(() -> strict.parse(tokenWith("auth-service", "someone-else")))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void ignoresIssuerAndAudienceWhenNotConfigured() {
        AuthenticatedUser user = jwtService.parse(tokenWith("anything", "anything"));

        assertThat(user.userId()).isEqualTo(userId);
    }

    @Test
    void extractsUserIdUsernameAndRoles() {
        String token = JwtTestTokens.valid(userId, "alice", List.of("MEMBER", "MODERATOR"));

        AuthenticatedUser user = jwtService.parseBearer(JwtTestTokens.bearer(token));

        assertThat(user.userId()).isEqualTo(userId);
        assertThat(user.username()).isEqualTo("alice");
        assertThat(user.roles()).containsExactlyInAnyOrder("MEMBER", "MODERATOR");
    }

    @Test
    void yieldsNoRolesWhenTheClaimIsAbsent() {
        String token = JwtTestTokens.signedWith(JwtTestTokens.SECRET, userId, "bob", null, Duration.ofMinutes(5));

        assertThat(jwtService.parse(token).roles()).isEmpty();
    }

    @Test
    void rejectsATokenSignedWithAnotherSecret() {
        String token = JwtTestTokens.signedWith(
                "another-secret-key-long-enough-for-hs256-signing-0123456789", userId, "mallory",
                Set.of(), Duration.ofMinutes(5));

        assertThatThrownBy(() -> jwtService.parse(token)).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void rejectsAnExpiredToken() {
        assertThatThrownBy(() -> jwtService.parse(JwtTestTokens.expired(userId)))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void derivesADeterministicUserIdFromANonUuidSubject() {
        // Mirrors cc-identity-service tokens: sub is the user's email, no userId/username/roles claims.
        String token = Jwts.builder()
                .subject("alice@example.com")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(5))))
                .signWith(Keys.hmacShaKeyFor(JwtTestTokens.SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        AuthenticatedUser first = jwtService.parse(token);
        AuthenticatedUser second = jwtService.parse(token);

        assertThat(first.userId()).isEqualTo(second.userId());
        assertThat(first.username()).isEqualTo("alice@example.com");
        assertThat(first.roles()).isEmpty();
    }

    @Test
    void prefersAUserIdClaimOverANonUuidSubject() {
        UUID claimedId = UUID.randomUUID();
        String token = Jwts.builder()
                .subject("alice@example.com")
                .claim("userId", claimedId.toString())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(5))))
                .signWith(Keys.hmacShaKeyFor(JwtTestTokens.SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThat(jwtService.parse(token).userId()).isEqualTo(claimedId);
    }

    @Test
    void rejectsAMalformedUserIdClaim() {
        String token = Jwts.builder()
                .subject("alice@example.com")
                .claim("userId", "not-a-uuid")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(5))))
                .signWith(Keys.hmacShaKeyFor(JwtTestTokens.SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThatThrownBy(() -> jwtService.parse(token)).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void rejectsATokenWithNoSubjectAtAll() {
        String token = Jwts.builder()
                .claim("username", "carol")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(5))))
                .signWith(Keys.hmacShaKeyFor(JwtTestTokens.SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThatThrownBy(() -> jwtService.parse(token)).isInstanceOf(InvalidTokenException.class);
    }

    @ParameterizedTest
    @NullSource
    // A header shorter than the prefix must be refused before it is ever sliced.
    @ValueSource(strings = {"Basic dXNlcjpwYXNz", "Bearer", "x", ""})
    void rejectsAMissingOrMalformedAuthorizationHeader(String header) {
        assertThatThrownBy(() -> jwtService.parseBearer(header)).isInstanceOf(InvalidTokenException.class);
    }
}
