package co.edu.escuelaing.techcup.communications.config;

import co.edu.escuelaing.techcup.communications.exception.InvalidTokenException;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WsAuthChannelInterceptorTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private MessageChannel channel;

    @InjectMocks
    private WsAuthChannelInterceptor interceptor;

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
        AuthenticatedUser user = new AuthenticatedUser(UUID.randomUUID(), "alice", Set.of("MEMBER"));
        StompHeaderAccessor accessor = accessorFor(StompCommand.CONNECT, "Bearer token");
        when(jwtService.parseBearer("Bearer token")).thenReturn(user);

        interceptor.preSend(messageFor(accessor), channel);

        assertThat(accessor.getUser()).isEqualTo(user);
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
}
