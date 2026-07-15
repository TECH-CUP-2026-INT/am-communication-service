package co.edu.escuelaing.techcup.communications.domain.service.ports.in;

import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;

import java.util.UUID;

public interface EscalateConversationUseCase {

    SupportTicket escalate(UUID ticketId, UUID callerId);
}
