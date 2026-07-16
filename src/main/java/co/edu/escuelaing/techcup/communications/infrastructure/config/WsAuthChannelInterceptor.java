package co.edu.escuelaing.techcup.communications.infrastructure.config;

import co.edu.escuelaing.techcup.communications.domain.exception.InvalidTokenException;
import co.edu.escuelaing.techcup.communications.domain.exception.SubscriptionNotAllowedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.lang.Nullable;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * Authenticates the STOMP session on its CONNECT frame, which carries the same
 * {@code Authorization: Bearer <jwt>} header as the REST calls. The resolved
 * {@link AuthenticatedUser} becomes the session principal for every later frame, and each
 * SUBSCRIBE is checked against the conversation it asks to listen to.
 */
@Component
@RequiredArgsConstructor
public class WsAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final SubscriptionAuthorizer subscriptionAuthorizer;
    private final WebSocketMetrics metrics;

    @Override
    @Nullable
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        switch (accessor.getCommand()) {
            case CONNECT -> accessor.setUser(authenticate(accessor));
            case SUBSCRIBE -> authorizeSubscription(accessor);
            default -> {
                // Every other frame reuses the principal established on CONNECT.
            }
        }
        return message;
    }

    private AuthenticatedUser authenticate(StompHeaderAccessor accessor) {
        try {
            return jwtService.parseBearer(accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION));
        } catch (InvalidTokenException ex) {
            metrics.recordAuthFailure();
            throw ex;
        }
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        try {
            subscriptionAuthorizer.authorize(accessor.getDestination(), principalOf(accessor));
        } catch (SubscriptionNotAllowedException ex) {
            metrics.recordSubscriptionDenied(accessor.getDestination());
            throw ex;
        }
    }

    private AuthenticatedUser principalOf(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof AuthenticatedUser subscriber) {
            return subscriber;
        }
        throw new InvalidTokenException("La sesión STOMP no está autenticada");
    }
}
