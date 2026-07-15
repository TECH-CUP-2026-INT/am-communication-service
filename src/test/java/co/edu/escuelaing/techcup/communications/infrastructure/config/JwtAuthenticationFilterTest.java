package co.edu.escuelaing.techcup.communications.infrastructure.config;

import co.edu.escuelaing.techcup.communications.domain.exception.InvalidTokenException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private SecurityErrorResponder errorResponder;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesTheCallerAndMapsRolesToAuthorities() throws Exception {
        AuthenticatedUser user = new AuthenticatedUser(UUID.randomUUID(), "alice", Set.of("MODERATOR"));
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");
        when(jwtService.parseBearer("Bearer token")).thenReturn(user);

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.getPrincipal()).isEqualTo(user);
        assertThat(authentication.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_MODERATOR");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void leavesTheRequestAnonymousWhenNoHeaderIsPresent() throws Exception {
        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, errorResponder);
    }

    @Test
    void answers401AndStopsTheChainOnAnInvalidToken() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer broken");
        when(jwtService.parseBearer("Bearer broken")).thenThrow(new InvalidTokenException("The provided JWT is not valid"));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(errorResponder).write(eq(request), eq(response), eq(HttpStatus.UNAUTHORIZED),
                eq("The provided JWT is not valid"));
        verify(filterChain, never()).doFilter(any(), any());
    }
}
