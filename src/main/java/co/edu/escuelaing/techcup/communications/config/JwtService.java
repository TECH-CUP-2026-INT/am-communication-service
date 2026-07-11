package co.edu.escuelaing.techcup.communications.config;

import co.edu.escuelaing.techcup.communications.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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
 */
@Service
public class JwtService {

    static final String BEARER_PREFIX = "Bearer ";
    private static final String USERNAME_CLAIM = "username";
    private static final String ROLES_CLAIM = "roles";

    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public AuthenticatedUser parseBearer(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new InvalidTokenException("Missing or malformed Authorization header");
        }
        return parse(authorizationHeader.substring(BEARER_PREFIX.length()));
    }

    public AuthenticatedUser parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new AuthenticatedUser(
                    UUID.fromString(claims.getSubject()),
                    claims.get(USERNAME_CLAIM, String.class),
                    roles(claims));
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException("The provided JWT is not valid");
        }
    }

    private Set<String> roles(Claims claims) {
        if (claims.get(ROLES_CLAIM) instanceof Collection<?> roles) {
            return roles.stream().map(String::valueOf).collect(Collectors.toUnmodifiableSet());
        }
        return Set.of();
    }
}
