package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.config.WebSocketMetrics;
import co.edu.escuelaing.techcup.communications.dto.SendMessageRequest;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.service.SendMessageUseCase;
import co.edu.escuelaing.techcup.communications.service.command.SendMessageCommand;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatWsControllerTest {

    @Mock
    private SendMessageUseCase sendMessageUseCase;

    @Mock
    private WebSocketMetrics metrics;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Tracer tracer;

    @InjectMocks
    private ChatWsController controller;

    @Test
    void delegatesToSendUseCaseWithTheSessionPrincipalAsSender() {
        UUID chatId = UUID.randomUUID();
        AuthenticatedUser caller = new AuthenticatedUser(
                UUID.randomUUID(), "alice", Set.of(ParticipantRole.MEMBER.name()));

        controller.send(new SendMessageRequest(chatId, "hello"), caller);

        ArgumentCaptor<SendMessageCommand> captor = ArgumentCaptor.forClass(SendMessageCommand.class);
        verify(sendMessageUseCase).send(captor.capture());
        verify(metrics).recordChatMessageReceived();
        assertThat(captor.getValue().chatId()).isEqualTo(chatId);
        assertThat(captor.getValue().senderId()).isEqualTo(caller.userId());
        assertThat(captor.getValue().content()).isEqualTo("hello");
    }
}
