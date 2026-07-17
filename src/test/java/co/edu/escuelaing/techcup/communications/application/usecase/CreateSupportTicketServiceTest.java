package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportTicketStatus;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ChatRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.SupportTicketRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.AuditServiceClient;
import co.edu.escuelaing.techcup.communications.application.usecase.command.CreateSupportTicketCommand;
import co.edu.escuelaing.techcup.communications.domain.service.support.SupportBotIdentity;
import co.edu.escuelaing.techcup.communications.domain.service.support.SupportChainOrchestrator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSupportTicketServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private SupportTicketRepository supportTicketRepository;

    @Mock
    private AuditServiceClient auditServiceClient;

    @Mock
    private SupportChainOrchestrator supportChainOrchestrator;

    @InjectMocks
    private CreateSupportTicketService service;

    private final UUID requester = UUID.randomUUID();

    @Test
    void createsSupportChatAndTicketAtFaqLevel() {
        when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));
        when(supportTicketRepository.save(any(SupportTicket.class))).thenAnswer(inv -> inv.getArgument(0));

        SupportTicket ticket = service.create(new CreateSupportTicketCommand(requester, "cannot login"));

        assertThat(ticket.getRequesterId()).isEqualTo(requester);
        assertThat(ticket.getStatus()).isEqualTo(SupportTicketStatus.OPEN);
        assertThat(ticket.getCurrentLevel()).isEqualTo(SupportLevel.FAQ);
        assertThat(ticket.getChat().isParticipant(requester)).isTrue();
        assertThat(ticket.getChat().isParticipant(SupportBotIdentity.BOT_USER_ID)).isTrue();
        verify(chatRepository).save(any(Chat.class));
        verify(auditServiceClient).recordEvent(eq("SUPPORT_TICKET_CREATED"), any(), any());
        verify(supportChainOrchestrator).runAutomatedStage(ticket);
    }
}
