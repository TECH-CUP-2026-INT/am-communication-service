package co.edu.escuelaing.techcup.communications.config;

import java.security.Principal;
import java.util.Set;
import java.util.UUID;

/**
 * Caller identity extracted from the JWT. It is the principal of both the REST
 * security context and the STOMP session, so it implements {@link Principal}.
 */
public record AuthenticatedUser(UUID userId, String username, Set<String> roles) implements Principal {

    public AuthenticatedUser {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    @Override
    public String getName() {
        return username;
    }
}
