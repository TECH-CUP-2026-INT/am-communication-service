package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import co.edu.escuelaing.techcup.communications.exception.ParticipantNotAllowedException;
import co.edu.escuelaing.techcup.communications.exception.SupportTicketNotFoundException;
import co.edu.escuelaing.techcup.communications.repository.ChatRepository;
import co.edu.escuelaing.techcup.communications.repository.SupportTicketRepository;
import co.edu.escuelaing.techcup.communications.service.EscalateConversationUseCase;
import co.edu.escuelaing.techcup.communications.service.support.SupportChainOrchestrator;
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
