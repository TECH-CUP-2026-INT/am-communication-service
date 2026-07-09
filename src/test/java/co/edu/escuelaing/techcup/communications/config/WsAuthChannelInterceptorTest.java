package co.edu.escuelaing.techcup.communications.config;

import co.edu.escuelaing.techcup.communications.exception.InvalidTokenException;
import co.edu.escuelaing.techcup.communications.exception.SubscriptionNotAllowedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WsAuthChannelInterceptorTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private SubscriptionAuthorizer subscriptionAuthorizer;

    @Mock
    private MessageChannel channel;

    @InjectMocks
    private WsAuthChannelInterceptor interceptor;

    private final AuthenticatedUser caller =
            new AuthenticatedUser(UUID.randomUUID(), "alice", Set.of("MEMBER"));

    private static StompHeaderAccessor accessorFor(StompCommand command, String authorization) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setLeaveMutable(true);
        if (authorization != null) {
            accessor.setNativeHeader(HttpHeaders.AUTHORIZATION, authorization);
        }
        return accessor;
    }

    private static Message<byte[]> messageFor(StompHeaderAccessor accessor) {
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    void connectFrameBecomesTheSessionPrincipal() {
        StompHeaderAccessor accessor = accessorFor(StompCommand.CONNECT, "Bearer token");
        when(jwtService.parseBearer("Bearer token")).thenReturn(caller);

        interceptor.preSend(messageFor(accessor), channel);

        assertThat(accessor.getUser()).isEqualTo(caller);
    }

    @Test
    void connectFrameWithoutATokenIsRejected() {
        StompHeaderAccessor accessor = accessorFor(StompCommand.CONNECT, null);
        when(jwtService.parseBearer(null)).thenThrow(new InvalidTokenException("Missing or malformed Authorization header"));

        Message<byte[]> message = messageFor(accessor);
        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void laterFramesReuseTheSessionPrincipalWithoutReparsingTheToken() {
        StompHeaderAccessor accessor = accessorFor(StompCommand.SEND, null);

        Message<byte[]> message = messageFor(accessor);
        assertThat(interceptor.preSend(message, channel)).isSameAs(message);
        verifyNoInteractions(jwtService);
    }

    @Test
    void subscribeFrameIsCheckedAgainstTheDestination() {
        String destination = "/topic/chat/" + UUID.randomUUID();
        StompHeaderAccessor accessor = accessorFor(StompCommand.SUBSCRIBE, null);
        accessor.setDestination(destination);
        accessor.setUser(caller);

        interceptor.preSend(messageFor(accessor), channel);

        verify(subscriptionAuthorizer).authorize(destination, caller);
        verifyNoInteractions(jwtService);
    }

    @Test
    void subscribeFrameToAForbiddenTopicIsRejected() {
        String destination = "/topic/chat/" + UUID.randomUUID();
        StompHeaderAccessor accessor = accessorFor(StompCommand.SUBSCRIBE, null);
        accessor.setDestination(destination);
        accessor.setUser(caller);
        doThrow(new SubscriptionNotAllowedException(caller.userId(), destination))
                .when(subscriptionAuthorizer).authorize(destination, caller);

        Message<byte[]> message = messageFor(accessor);
        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(SubscriptionNotAllowedException.class);
    }

    @Test
    void subscribeFrameOnAnUnauthenticatedSessionIsRejected() {
        StompHeaderAccessor accessor = accessorFor(StompCommand.SUBSCRIBE, null);
        accessor.setDestination("/topic/chat/" + UUID.randomUUID());

        Message<byte[]> message = messageFor(accessor);
        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(InvalidTokenException.class);
        verifyNoInteractions(subscriptionAuthorizer);
    }
}
