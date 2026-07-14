package co.edu.escuelaing.techcup.communications.config;

import co.edu.escuelaing.techcup.communications.exception.SubscriptionNotAllowedException;
import co.edu.escuelaing.techcup.communications.service.ChatAccessUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionAuthorizerTest {

    @Mock
    private ChatAccessUseCase chatAccess;

    private SubscriptionAuthorizer authorizer;

    private final UUID chatId = UUID.randomUUID();
    private final UUID ticketId = UUID.randomUUID();
    private final AuthenticatedUser caller =
            new AuthenticatedUser(UUID.randomUUID(), "alice", Set.of("MEMBER"));

    @BeforeEach
    void setUp() {
        authorizer = new SubscriptionAuthorizer(chatAccess);
    }

    @Test
    void letsAParticipantSubscribeToTheChatTopic() {
        when(chatAccess.canReadChat(chatId, caller.userId())).thenReturn(true);

        assertThatCode(() -> authorizer.authorize("/topic/chat/" + chatId, caller))
                .doesNotThrowAnyException();
    }

    @Test
    void refusesTheChatTopicToANonParticipant() {
        when(chatAccess.canReadChat(chatId, caller.userId())).thenReturn(false);

        assertThatThrownBy(() -> authorizer.authorize("/topic/chat/" + chatId, caller))
                .isInstanceOf(SubscriptionNotAllowedException.class);
    }

    @Test
    void letsAParticipantSubscribeToTheSupportTopic() {
        when(chatAccess.canReadSupportTicket(ticketId, caller.userId())).thenReturn(true);

        assertThatCode(() -> authorizer.authorize("/topic/support/" + ticketId, caller))
                .doesNotThrowAnyException();
    }

    @Test
    void refusesTheSupportTopicToAStranger() {
        when(chatAccess.canReadSupportTicket(ticketId, caller.userId())).thenReturn(false);

        assertThatThrownBy(() -> authorizer.authorize("/topic/support/" + ticketId, caller))
                .isInstanceOf(SubscriptionNotAllowedException.class);
    }

    @ParameterizedTest
    @NullSource
    // An unknown, malformed or over-long destination is refused before any lookup happens.
    @ValueSource(strings = {
            "",
            "/topic/other/" + "00000000-0000-0000-0000-000000000000",
            "/topic/chat/not-a-uuid",
            "/topic/chat/",
            "/topic/chat/00000000-0000-0000-0000-000000000000/messages"
    })
    void refusesADestinationItCannotResolve(String destination) {
        assertThatThrownBy(() -> authorizer.authorize(destination, caller))
                .isInstanceOf(SubscriptionNotAllowedException.class);
        verifyNoInteractions(chatAccess);
    }
}
