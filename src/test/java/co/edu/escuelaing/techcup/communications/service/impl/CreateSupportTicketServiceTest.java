package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import co.edu.escuelaing.techcup.communications.entity.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.entity.enums.SupportTicketStatus;
import co.edu.escuelaing.techcup.communications.repository.ChatRepository;
import co.edu.escuelaing.techcup.communications.repository.SupportTicketRepository;
import co.edu.escuelaing.techcup.communications.service.client.AuditServiceClient;
import co.edu.escuelaing.techcup.communications.service.command.CreateSupportTicketCommand;
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

    @InjectMocks
    private CreateSupportTicketService service;

    private final UUID requester = UUID.randomUUID();

    @Test
    void createsSupportChatAndTicketAtChatbotLevel() {
        when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));
        when(supportTicketRepository.save(any(SupportTicket.class))).thenAnswer(inv -> inv.getArgument(0));

        SupportTicket ticket = service.create(new CreateSupportTicketCommand(requester, "cannot login"));

        assertThat(ticket.getRequesterId()).isEqualTo(requester);
        assertThat(ticket.getStatus()).isEqualTo(SupportTicketStatus.OPEN);
        assertThat(ticket.getCurrentLevel()).isEqualTo(SupportLevel.CHATBOT);
        verify(chatRepository).save(any(Chat.class));
        verify(auditServiceClient).record(eq("SUPPORT_TICKET_CREATED"), any(), any());
    }
}
