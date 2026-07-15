package co.edu.escuelaing.techcup.communications.infrastructure.config;

import co.edu.escuelaing.techcup.communications.domain.exception.InvalidTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates REST calls from the {@code Authorization: Bearer <jwt>} header. A request without
 * the header stays anonymous and is rejected later by the authorization rules, so that public
 * endpoints (the WebSocket handshake) keep working.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final SecurityErrorResponder errorResponder;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null) {
            chain.doFilter(request, response);
            return;
        }
        try {
            SecurityContextHolder.getContext().setAuthentication(authenticationFor(jwtService.parseBearer(header)));
        } catch (InvalidTokenException ex) {
            SecurityContextHolder.clearContext();
            errorResponder.write(request, response, HttpStatus.UNAUTHORIZED, ex.getMessage());
            return;
        }
        chain.doFilter(request, response);
    }

    private Authentication authenticationFor(AuthenticatedUser user) {
        List<SimpleGrantedAuthority> authorities = user.roles().stream()
                .map(SecurityRoles::authorityOf)
                .toList();
        return new UsernamePasswordAuthenticationToken(user, null, authorities);
    }
}
