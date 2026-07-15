package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ChatRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.SupportTicketRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.CreateSupportTicketUseCase;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.AuditServiceClient;
import co.edu.escuelaing.techcup.communications.application.usecase.command.CreateSupportTicketCommand;
import co.edu.escuelaing.techcup.communications.domain.service.support.SupportBotIdentity;
import co.edu.escuelaing.techcup.communications.domain.service.support.SupportChainOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateSupportTicketService implements CreateSupportTicketUseCase {

    private final ChatRepository chatRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final AuditServiceClient auditServiceClient;
    private final SupportChainOrchestrator supportChainOrchestrator;

    @Override
    @Transactional
    public SupportTicket create(CreateSupportTicketCommand command) {
        Chat chat = Chat.create(ChatType.SUPPORT, null);
        chat.addParticipant(command.requesterId(), ParticipantRole.MEMBER);
        chat.addParticipant(SupportBotIdentity.BOT_USER_ID, ParticipantRole.MEMBER);
        chatRepository.save(chat);

        SupportTicket ticket = supportTicketRepository.save(
                SupportTicket.open(chat, command.requesterId(), command.subject()));

        auditServiceClient.record("SUPPORT_TICKET_CREATED", ticket.getId(),
                "requester=" + command.requesterId());

        supportChainOrchestrator.runAutomatedStage(ticket);
        return supportTicketRepository.save(ticket);
    }
}
