package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.exception.SupportTicketNotFoundException;
import co.edu.escuelaing.techcup.communications.repository.ChatRepository;
import co.edu.escuelaing.techcup.communications.repository.MessageRepository;
import co.edu.escuelaing.techcup.communications.repository.SupportTicketRepository;
import co.edu.escuelaing.techcup.communications.service.client.MessagePublisher;
import co.edu.escuelaing.techcup.communications.service.command.ReplySupportTicketCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplySupportTicketServiceTest {

    @Mock
    private SupportTicketRepository supportTicketRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MessagePublisher messagePublisher;

    @InjectMocks
    private ReplySupportTicketService service;

    private final UUID requester = UUID.randomUUID();

    private SupportTicket ticketWithRequester() {
        Chat chat = Chat.create(ChatType.SUPPORT, null);
        chat.addParticipant(requester, ParticipantRole.MEMBER);
        return SupportTicket.open(chat, requester, "issue");
    }

    @Test
    void requesterRepliesWithoutRejoining() {
        SupportTicket ticket = ticketWithRequester();
        when(supportTicketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        Message message = service.reply(new ReplySupportTicketCommand(ticket.getId(), requester, "any update?"));

        assertThat(message.getContent()).isEqualTo("any update?");
        verify(chatRepository, never()).save(any());
        verify(messagePublisher).publishSupportMessage(ticket.getId(), message);
    }

    @Test
    void agentJoinsConversationOnFirstReply() {
        SupportTicket ticket = ticketWithRequester();
        UUID agent = UUID.randomUUID();
        when(supportTicketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        Message message = service.reply(new ReplySupportTicketCommand(ticket.getId(), agent, "how can I help?"));

        assertThat(message.getSenderId()).isEqualTo(agent);
        assertThat(ticket.getChat().isParticipant(agent)).isTrue();
        verify(chatRepository).save(ticket.getChat());
    }

    @Test
    void throwsWhenTicketNotFound() {
        UUID id = UUID.randomUUID();
        when(supportTicketRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reply(new ReplySupportTicketCommand(id, requester, "hi")))
                .isInstanceOf(SupportTicketNotFoundException.class);
        verify(messageRepository, never()).save(any());
    }
}
