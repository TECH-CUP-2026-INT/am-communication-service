package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.config.AuthenticatedUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/**
 * Puts an {@link AuthenticatedUser} in the security context, as the JWT filter would.
 * The controller slices run without filters, so the context is populated directly.
 */
final class TestCallers {

    private TestCallers() {
    }

    static RequestPostProcessor caller(UUID userId, String... roles) {
        AuthenticatedUser user = new AuthenticatedUser(userId, "tester", Set.of(roles));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
                Arrays.stream(roles).map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList());
        return request -> {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return request;
        };
    }
}
