package co.edu.escuelaing.techcup.communications.domain.model;

import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;
import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportTicketStatus;
import co.edu.escuelaing.techcup.communications.domain.exception.InvalidChatOperationException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupportTicketTest {

    private final UUID requester = UUID.randomUUID();

    private SupportTicket newTicket() {
        Chat chat = Chat.create(ChatType.SUPPORT, null);
        return SupportTicket.open(chat, requester, "Cannot join tournament");
    }

    @Test
    void opensAtFaqLevelAndOpenStatus() {
        SupportTicket ticket = newTicket();

        assertThat(ticket.getId()).isNotNull();
        assertThat(ticket.getStatus()).isEqualTo(SupportTicketStatus.OPEN);
        assertThat(ticket.getCurrentLevel()).isEqualTo(SupportLevel.FAQ);
        assertThat(ticket.getRequesterId()).isEqualTo(requester);
        assertThat(ticket.getChatId()).isNotNull();
    }

    @Test
    void rejectsBlankSubject() {
        Chat chat = Chat.create(ChatType.SUPPORT, null);

        assertThatThrownBy(() -> SupportTicket.open(chat, requester, "  "))
                .isInstanceOf(InvalidChatOperationException.class);
    }

    @Test
    void escalatesToNextLevelAndFlagsEscalated() {
        SupportTicket ticket = newTicket();

        ticket.escalateTo(SupportLevel.CHATBOT);

        assertThat(ticket.getCurrentLevel()).isEqualTo(SupportLevel.CHATBOT);
        assertThat(ticket.getStatus()).isEqualTo(SupportTicketStatus.ESCALATED);
    }

    @Test
    void assignsAgentAndMovesToInProgress() {
        SupportTicket ticket = newTicket();
        UUID agent = UUID.randomUUID();

        ticket.assignTo(agent);

        assertThat(ticket.getAssignedTo()).isEqualTo(agent);
        assertThat(ticket.getStatus()).isEqualTo(SupportTicketStatus.IN_PROGRESS);
    }

    @Test
    void resolvesTicket() {
        SupportTicket ticket = newTicket();

        ticket.resolve();

        assertThat(ticket.isResolved()).isTrue();
        assertThat(ticket.getStatus()).isEqualTo(SupportTicketStatus.RESOLVED);
    }

    @Test
    void marksPending() {
        SupportTicket ticket = newTicket();

        ticket.markPending();

        assertThat(ticket.getCurrentLevel()).isEqualTo(SupportLevel.PENDING);
        assertThat(ticket.getStatus()).isEqualTo(SupportTicketStatus.PENDING);
    }

    @Test
    void resolvedTicketCannotBeEscalated() {
        SupportTicket ticket = newTicket();
        ticket.resolve();

        assertThatThrownBy(() -> ticket.escalateTo(SupportLevel.MODERATOR))
                .isInstanceOf(InvalidChatOperationException.class);
    }

    @Test
    void updatedAtAdvancesOnTransition() {
        SupportTicket ticket = newTicket();

        ticket.escalateTo(SupportLevel.CHATBOT);

        assertThat(ticket.getUpdatedAt()).isAfterOrEqualTo(ticket.getCreatedAt());
    }
}
