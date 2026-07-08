package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.repository.ChatRepository;
import co.edu.escuelaing.techcup.communications.repository.SupportTicketRepository;
import co.edu.escuelaing.techcup.communications.service.CreateSupportTicketUseCase;
import co.edu.escuelaing.techcup.communications.service.client.AuditServiceClient;
import co.edu.escuelaing.techcup.communications.service.command.CreateSupportTicketCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateSupportTicketService implements CreateSupportTicketUseCase {

    private final ChatRepository chatRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final AuditServiceClient auditServiceClient;

    @Override
    @Transactional
    public SupportTicket create(CreateSupportTicketCommand command) {
        Chat chat = Chat.create(ChatType.SUPPORT, null);
        chat.addParticipant(command.requesterId(), ParticipantRole.MEMBER);
        chatRepository.save(chat);

        SupportTicket ticket = supportTicketRepository.save(
                SupportTicket.open(chat, command.requesterId(), command.subject()));

        auditServiceClient.record("SUPPORT_TICKET_CREATED", ticket.getId(),
                "requester=" + command.requesterId());
        return ticket;
    }
}
