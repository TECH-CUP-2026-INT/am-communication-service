package co.edu.escuelaing.techcup.communications.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Bridges the {@code roles} claim of the JWT with Spring Security authorities, which expect the
 * {@code ROLE_} prefix. Tokens may carry either form.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SecurityRoles {

    private static final String ROLE_PREFIX = "ROLE_";

    static SimpleGrantedAuthority authorityOf(String role) {
        return new SimpleGrantedAuthority(role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role);
    }
}
