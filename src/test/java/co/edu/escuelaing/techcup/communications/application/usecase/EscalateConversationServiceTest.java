package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.domain.exception.ParticipantNotAllowedException;
import co.edu.escuelaing.techcup.communications.domain.exception.SupportTicketNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ChatRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.SupportTicketRepository;
import co.edu.escuelaing.techcup.communications.domain.service.support.SupportChainOrchestrator;
import co.edu.escuelaing.techcup.communications.domain.service.support.SupportResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EscalateConversationServiceTest {

    @Mock
    private SupportTicketRepository supportTicketRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private SupportChainOrchestrator supportChainOrchestrator;

    @InjectMocks
    private EscalateConversationService service;

    private final UUID caller = UUID.randomUUID();

    private SupportTicket newTicket() {
        Chat chat = Chat.create(ChatType.SUPPORT, null);
        chat.addParticipant(caller, ParticipantRole.MEMBER);
        return SupportTicket.open(chat, UUID.randomUUID(), "issue");
    }

    @Test
    void runsChainAndPersistsWhenCallerIsParticipant() {
        SupportTicket ticket = newTicket();
        when(supportTicketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(chatRepository.isParticipant(ticket.getChatId(), caller)).thenReturn(true);
        when(supportTicketRepository.save(ticket)).thenReturn(ticket);
        when(supportChainOrchestrator.escalate(ticket)).thenReturn(SupportResult.pending(ticket.getCurrentLevel()));

        service.escalate(ticket.getId(), caller);

        verify(supportChainOrchestrator).escalate(ticket);
        verify(supportTicketRepository).save(ticket);
    }

    @Test
    void throwsWhenTicketNotFound() {
        UUID id = UUID.randomUUID();
        when(supportTicketRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.escalate(id, caller)).isInstanceOf(SupportTicketNotFoundException.class);
        verify(supportChainOrchestrator, never()).escalate(any());
    }

    @Test
    void throwsWhenCallerIsNotAParticipant() {
        SupportTicket ticket = newTicket();
        when(supportTicketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(chatRepository.isParticipant(ticket.getChatId(), caller)).thenReturn(false);

        assertThatThrownBy(() -> service.escalate(ticket.getId(), caller))
                .isInstanceOf(ParticipantNotAllowedException.class);
        verify(supportChainOrchestrator, never()).escalate(any());
    }
}
