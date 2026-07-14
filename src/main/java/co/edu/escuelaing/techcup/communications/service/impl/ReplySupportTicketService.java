package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.exception.SupportTicketNotFoundException;
import co.edu.escuelaing.techcup.communications.repository.ChatRepository;
import co.edu.escuelaing.techcup.communications.repository.MessageRepository;
import co.edu.escuelaing.techcup.communications.repository.SupportTicketRepository;
import co.edu.escuelaing.techcup.communications.service.ReplySupportTicketUseCase;
import co.edu.escuelaing.techcup.communications.service.client.MessagePublisher;
import co.edu.escuelaing.techcup.communications.service.command.ReplySupportTicketCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReplySupportTicketService implements ReplySupportTicketUseCase {

    private final SupportTicketRepository supportTicketRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final MessagePublisher messagePublisher;

    @Override
    @Transactional
    public Message reply(ReplySupportTicketCommand command) {
        SupportTicket ticket = supportTicketRepository.findById(command.ticketId())
                .orElseThrow(() -> new SupportTicketNotFoundException(command.ticketId()));

        Chat chat = ticket.getChat();
        // Support agents join the conversation the first time they reply.
        if (!chat.isParticipant(command.senderId())) {
            chat.addParticipant(command.senderId(), ParticipantRole.MEMBER);
            chatRepository.save(chat);
        }
        // Persist first, then publish to the ticket's real-time topic.
        Message saved = messageRepository.save(chat.postMessage(command.senderId(), command.content()));
        messagePublisher.publishSupportMessage(ticket.getId(), saved);
        return saved;
    }
}
