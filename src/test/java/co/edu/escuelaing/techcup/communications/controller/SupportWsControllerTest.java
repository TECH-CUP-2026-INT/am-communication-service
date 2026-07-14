package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.config.WebSocketMetrics;
import co.edu.escuelaing.techcup.communications.dto.SupportSendRequest;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.service.ReplySupportTicketUseCase;
import co.edu.escuelaing.techcup.communications.service.command.ReplySupportTicketCommand;
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
class SupportWsControllerTest {

    @Mock
    private ReplySupportTicketUseCase replySupportTicketUseCase;

    @Mock
    private WebSocketMetrics metrics;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Tracer tracer;

    @InjectMocks
    private SupportWsController controller;

    @Test
    void delegatesToReplyUseCaseWithTheSessionPrincipalAsSender() {
        UUID ticketId = UUID.randomUUID();
        AuthenticatedUser caller = new AuthenticatedUser(
                UUID.randomUUID(), "agent", Set.of(ParticipantRole.MODERATOR.name()));

        controller.send(new SupportSendRequest(ticketId, "on it"), caller);

        ArgumentCaptor<ReplySupportTicketCommand> captor = ArgumentCaptor.forClass(ReplySupportTicketCommand.class);
        verify(replySupportTicketUseCase).reply(captor.capture());
        verify(metrics).recordSupportMessageReceived();
        assertThat(captor.getValue().ticketId()).isEqualTo(ticketId);
        assertThat(captor.getValue().senderId()).isEqualTo(caller.userId());
        assertThat(captor.getValue().content()).isEqualTo("on it");
    }
}
