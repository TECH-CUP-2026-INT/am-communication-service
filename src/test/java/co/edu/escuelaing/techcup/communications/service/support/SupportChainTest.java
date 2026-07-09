package co.edu.escuelaing.techcup.communications.service.support;

import co.edu.escuelaing.techcup.communications.config.SupportChainConfig;
import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.entity.enums.SupportOutcome;
import co.edu.escuelaing.techcup.communications.service.client.AuditServiceClient;
import co.edu.escuelaing.techcup.communications.service.client.NotificationServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class SupportChainTest {

    private SupportHandler head;
    private AuditServiceClient audit;
    private NotificationServiceClient notifications;
    private SupportChainOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        head = new SupportChainConfig().supportChainHead();
        audit = Mockito.mock(AuditServiceClient.class);
        notifications = Mockito.mock(NotificationServiceClient.class);
        orchestrator = new SupportChainOrchestrator(head, audit, notifications);
    }

    private SupportTicket newTicket() {
        Chat chat = Chat.create(ChatType.SUPPORT, null);
        return SupportTicket.open(chat, UUID.randomUUID(), "issue");
    }

    @Test
    void chatbotHandlerOnlyHandlesChatbotLevel() {
        SupportHandler chatbot = new ChatbotSupportHandler();
        SupportTicket ticket = newTicket();

        assertThat(chatbot.canHandle(ticket)).isTrue();
        ticket.escalateTo(SupportLevel.MODERATOR);
        assertThat(chatbot.canHandle(ticket)).isFalse();
    }

    @Test
    void escalatesChatbotToAutomatic() {
        SupportTicket ticket = newTicket();

        SupportResult result = orchestrator.process(ticket);

        assertThat(result.outcome()).isEqualTo(SupportOutcome.ESCALATED);
        assertThat(result.from()).isEqualTo(SupportLevel.CHATBOT);
        assertThat(result.to()).isEqualTo(SupportLevel.AUTOMATIC);
        assertThat(ticket.getCurrentLevel()).isEqualTo(SupportLevel.AUTOMATIC);
        verify(audit).record(eq("SUPPORT_TRANSITION"), eq(ticket.getId()), any());
        verify(notifications).notify(eq(ticket.getRequesterId()), any(), any());
    }

    @Test
    void walksTheFullEscalationLadderToPending() {
        SupportTicket ticket = newTicket();

        orchestrator.process(ticket); // CHATBOT -> AUTOMATIC
        assertThat(ticket.getCurrentLevel()).isEqualTo(SupportLevel.AUTOMATIC);

        orchestrator.process(ticket); // AUTOMATIC -> MODERATOR
        assertThat(ticket.getCurrentLevel()).isEqualTo(SupportLevel.MODERATOR);

        orchestrator.process(ticket); // MODERATOR -> ORGANIZER
        assertThat(ticket.getCurrentLevel()).isEqualTo(SupportLevel.ORGANIZER);

        SupportResult finalStep = orchestrator.process(ticket); // ORGANIZER -> PENDING (finalized)
        assertThat(finalStep.outcome()).isEqualTo(SupportOutcome.FINALIZED);
        assertThat(ticket.getCurrentLevel()).isEqualTo(SupportLevel.PENDING);

        verify(audit, times(4)).record(eq("SUPPORT_TRANSITION"), eq(ticket.getId()), any());
    }

    @Test
    void pendingTicketHasNoHandler() {
        SupportTicket ticket = newTicket();
        ticket.markPending();

        SupportResult result = orchestrator.process(ticket);

        assertThat(result.outcome()).isEqualTo(SupportOutcome.PENDING);
        assertThat(ticket.getCurrentLevel()).isEqualTo(SupportLevel.PENDING);
    }
}
