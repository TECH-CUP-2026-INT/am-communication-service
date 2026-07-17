package co.edu.escuelaing.techcup.communications.infrastructure.config;

import co.edu.escuelaing.techcup.communications.domain.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Validates HS256 tokens issued by the authentication service and extracts the caller
 * identity. This service never issues tokens: login lives outside this microservice.
 *
 * <p>{@link #parse} accepts three shapes for the caller id, in order: a UUID {@code sub}; a UUID
 * {@code userId} claim; or, as a last resort, a UUID derived deterministically from a non-UUID
 * {@code sub} (same subject always yields the same id). cc-identity-service now puts the real
 * UUID in {@code sub}, so this only exists for tokens issued elsewhere that don't.
 *
 * <p>Roles: cc-identity-service issues a single {@code role} string claim (e.g. {@code "ADMIN"}),
 * not a {@code roles} array. Both shapes are accepted so this service works against either.
 */
@Service
public class JwtService {

    static final String BEARER_PREFIX = "Bearer ";
    private static final String USERNAME_CLAIM = "username";
    private static final String USER_ID_CLAIM = "userId";
    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_CLAIM = "role";

    private final JwtParser parser;

    public JwtService(JwtProperties properties) {
        SecretKey signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        JwtParserBuilder builder = Jwts.parser()
                .verifyWith(signingKey)
                .clockSkewSeconds(properties.clockSkewSeconds());
        if (properties.issuer() != null) {
            builder.requireIssuer(properties.issuer());
        }
        if (properties.audience() != null) {
            builder.requireAudience(properties.audience());
        }
        this.parser = builder.build();
    }

    public AuthenticatedUser parseBearer(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new InvalidTokenException("El encabezado de autorización falta o está mal formado");
        }
        return parse(authorizationHeader.substring(BEARER_PREFIX.length()));
    }

    public AuthenticatedUser parse(String token) {
        try {
            Claims claims = parser.parseSignedClaims(token).getPayload();
            return new AuthenticatedUser(resolveUserId(claims), resolveUsername(claims), roles(claims));
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException("El token JWT proporcionado no es válido");
        }
    }

    private UUID resolveUserId(Claims claims) {
        String subject = claims.getSubject();
        if (isUuid(subject)) {
            return UUID.fromString(subject);
        }
        String userIdClaim = claims.get(USER_ID_CLAIM, String.class);
        if (userIdClaim != null) {
            return UUID.fromString(userIdClaim);
        }
        if (subject == null || subject.isBlank()) {
            throw new InvalidTokenException("El token JWT proporcionado no es válido");
        }
        return UUID.nameUUIDFromBytes(subject.getBytes(StandardCharsets.UTF_8));
    }

    private String resolveUsername(Claims claims) {
        String username = claims.get(USERNAME_CLAIM, String.class);
        return username != null ? username : claims.getSubject();
    }

    private boolean isUuid(String value) {
        if (value == null) {
            return false;
        }
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private Set<String> roles(Claims claims) {
        if (claims.get(ROLES_CLAIM) instanceof Collection<?> roles) {
            return roles.stream().map(String::valueOf).collect(Collectors.toUnmodifiableSet());
        }
        String singleRole = claims.get(ROLE_CLAIM, String.class);
        if (singleRole != null) {
            return Set.of(singleRole);
        }
        return Set.of();
    }
}
