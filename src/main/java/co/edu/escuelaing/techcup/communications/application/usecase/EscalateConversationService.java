package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import co.edu.escuelaing.techcup.communications.domain.exception.ParticipantNotAllowedException;
import co.edu.escuelaing.techcup.communications.domain.exception.SupportTicketNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ChatRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.SupportTicketRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.EscalateConversationUseCase;
import co.edu.escuelaing.techcup.communications.domain.service.support.SupportChainOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EscalateConversationService implements EscalateConversationUseCase {

    private final SupportTicketRepository supportTicketRepository;
    private final ChatRepository chatRepository;
    private final SupportChainOrchestrator supportChainOrchestrator;

    @Override
    @Transactional
    public SupportTicket escalate(UUID ticketId, UUID callerId) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new SupportTicketNotFoundException(ticketId));
        if (!chatRepository.isParticipant(ticket.getChatId(), callerId)) {
            throw new ParticipantNotAllowedException(callerId, ticket.getChatId());
        }
        // The chain mutates the ticket and audits the transition.
        supportChainOrchestrator.escalate(ticket);
        return supportTicketRepository.save(ticket);
    }
}
