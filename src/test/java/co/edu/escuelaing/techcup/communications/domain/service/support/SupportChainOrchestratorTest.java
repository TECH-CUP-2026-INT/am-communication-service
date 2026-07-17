package co.edu.escuelaing.techcup.communications.domain.service.support;

import co.edu.escuelaing.techcup.communications.domain.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;
import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportOutcome;
import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportTicketStatus;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.AuditServiceClient;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.NotificationServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class SupportChainOrchestratorTest {

    /** Thin fakes standing in for the real FAQ/chatbot handlers, which need a MessageRepository
     * and MessagePublisher this test doesn't otherwise need to wire up. Their level and
     * escalation behavior match the real handlers exactly (FAQ never advances the ticket on its
     * own; chatbot always escalates to MODERATOR). */
    private static class FakeFaqHandler extends AbstractSupportHandler {
        @Override
        protected SupportLevel level() {
            return SupportLevel.FAQ;
        }

        @Override
        protected SupportResult doHandle(SupportTicket ticket) {
            return SupportResult.resolved(SupportLevel.FAQ);
        }
    }

    private static class FakeChatbotHandler extends AbstractSupportHandler {
        @Override
        protected SupportLevel level() {
            return SupportLevel.CHATBOT;
        }

        @Override
        protected SupportResult doHandle(SupportTicket ticket) {
            ticket.escalateTo(SupportLevel.MODERATOR);
            return SupportResult.escalated(SupportLevel.CHATBOT, SupportLevel.MODERATOR);
        }
    }

    private AuditServiceClient audit;
    private NotificationServiceClient notifications;
    private SupportChainOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        SupportHandler faq = new FakeFaqHandler();
        SupportHandler chatbot = new FakeChatbotHandler();
        SupportHandler moderator = new ModeratorSupportHandler();
        SupportHandler organizer = new OrganizerSupportHandler();
        faq.setNext(chatbot);
        chatbot.setNext(moderator);
        moderator.setNext(organizer);

        audit = mock(AuditServiceClient.class);
        notifications = mock(NotificationServiceClient.class);
        orchestrator = new SupportChainOrchestrator(faq, audit, notifications);
    }

    private SupportTicket newTicket() {
        Chat chat = Chat.create(ChatType.SUPPORT, null);
        return SupportTicket.open(chat, UUID.randomUUID(), "issue");
    }

    @Test
    void runAutomatedStageAnswersFaqWithoutAdvancingTheLevel() {
        SupportTicket ticket = newTicket();

        SupportResult result = orchestrator.runAutomatedStage(ticket);

        assertThat(result.outcome()).isEqualTo(SupportOutcome.RESOLVED);
        assertThat(ticket.getCurrentLevel()).isEqualTo(SupportLevel.FAQ);
        verify(audit).recordEvent(eq("SUPPORT_TRANSITION"), eq(ticket.getId()), any());
        verify(notifications).notify(eq(ticket.getRequesterId()), any(), any());
    }

    @Test
    void runAutomatedStageSurvivesANotificationFailure() {
        SupportTicket ticket = newTicket();
        doThrow(new IntegrationException("notification service", new RuntimeException("404")))
                .when(notifications).notify(any(), any(), any());

        assertThatCode(() -> {
            SupportResult result = orchestrator.runAutomatedStage(ticket);
            assertThat(result.outcome()).isEqualTo(SupportOutcome.RESOLVED);
        }).doesNotThrowAnyException();

        verify(audit).recordEvent(eq("SUPPORT_TRANSITION"), eq(ticket.getId()), any());
    }

    @Test
    void escalatingFromFaqLandsOnChatbotAndRunsItInTheSameCall() {
        SupportTicket ticket = newTicket();

        SupportResult result = orchestrator.escalate(ticket);

        assertThat(ticket.getCurrentLevel()).isEqualTo(SupportLevel.MODERATOR);
        assertThat(result.outcome()).isEqualTo(SupportOutcome.ESCALATED);
        assertThat(result.from()).isEqualTo(SupportLevel.CHATBOT);
        assertThat(result.to()).isEqualTo(SupportLevel.MODERATOR);
        // Two real transitions in one call: FAQ->CHATBOT (forced) then CHATBOT->MODERATOR (automated).
        verify(audit, times(2)).recordEvent(eq("SUPPORT_TRANSITION"), eq(ticket.getId()), any());
    }

    @Test
    void escalatingFromModeratorLandsOnOrganizerWithoutFurtherCascade() {
        SupportTicket ticket = newTicket();
        ticket.escalateTo(SupportLevel.MODERATOR);

        SupportResult result = orchestrator.escalate(ticket);

        assertThat(ticket.getCurrentLevel()).isEqualTo(SupportLevel.ORGANIZER);
        assertThat(result.outcome()).isEqualTo(SupportOutcome.ESCALATED);
        verify(audit, times(1)).recordEvent(eq("SUPPORT_TRANSITION"), eq(ticket.getId()), any());
    }

    @Test
    void escalatingFromOrganizerFinalizesAsPending() {
        SupportTicket ticket = newTicket();
        ticket.escalateTo(SupportLevel.ORGANIZER);

        SupportResult result = orchestrator.escalate(ticket);

        assertThat(ticket.getCurrentLevel()).isEqualTo(SupportLevel.PENDING);
        assertThat(ticket.getStatus()).isEqualTo(SupportTicketStatus.PENDING);
        assertThat(result.outcome()).isEqualTo(SupportOutcome.FINALIZED);
        verify(audit, times(1)).recordEvent(eq("SUPPORT_TRANSITION"), eq(ticket.getId()), any());
    }

    @Test
    void escalatingAPendingTicketDoesNotMutateItOrFlipItBackToEscalated() {
        SupportTicket ticket = newTicket();
        ticket.markPending();

        SupportResult result = orchestrator.escalate(ticket);

        assertThat(ticket.getCurrentLevel()).isEqualTo(SupportLevel.PENDING);
        assertThat(ticket.getStatus()).isEqualTo(SupportTicketStatus.PENDING);
        assertThat(result.outcome()).isEqualTo(SupportOutcome.PENDING);
        verify(audit, times(1)).recordEvent(eq("SUPPORT_TRANSITION"), eq(ticket.getId()), any());
    }
}
