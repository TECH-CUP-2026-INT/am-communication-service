package co.edu.escuelaing.techcup.communications.config;

import co.edu.escuelaing.techcup.communications.exception.InvalidTokenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(new JwtProperties(JwtTestTokens.SECRET));

    private final UUID userId = UUID.randomUUID();

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
    void rejectsASubjectThatIsNotAUuid() {
        String token = JwtTestTokens.signedWith(JwtTestTokens.SECRET, null, "carol", Set.of(), Duration.ofMinutes(5));

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
