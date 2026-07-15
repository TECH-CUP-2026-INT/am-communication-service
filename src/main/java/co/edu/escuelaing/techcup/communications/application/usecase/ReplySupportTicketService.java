package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.Message;
import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.domain.exception.SupportTicketNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ChatRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.MessageRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.SupportTicketRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.ReplySupportTicketUseCase;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.MessagePublisher;
import co.edu.escuelaing.techcup.communications.application.usecase.command.ReplySupportTicketCommand;
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
