package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.dto.SupportSendRequest;
import co.edu.escuelaing.techcup.communications.service.ReplySupportTicketUseCase;
import co.edu.escuelaing.techcup.communications.service.command.ReplySupportTicketCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SupportWsControllerTest {

    @Mock
    private ReplySupportTicketUseCase replySupportTicketUseCase;

    @InjectMocks
    private SupportWsController controller;

    @Test
    void delegatesToReplyUseCase() {
        UUID ticketId = UUID.randomUUID();
        UUID sender = UUID.randomUUID();
        controller.send(new SupportSendRequest(ticketId, sender, "on it"));

        ArgumentCaptor<ReplySupportTicketCommand> captor = ArgumentCaptor.forClass(ReplySupportTicketCommand.class);
        verify(replySupportTicketUseCase).reply(captor.capture());
        assertThat(captor.getValue().ticketId()).isEqualTo(ticketId);
        assertThat(captor.getValue().senderId()).isEqualTo(sender);
        assertThat(captor.getValue().content()).isEqualTo("on it");
    }
}
