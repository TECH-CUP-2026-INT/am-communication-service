package co.edu.escuelaing.techcup.communications.service;

import co.edu.escuelaing.techcup.communications.entity.SupportTicket;

import java.util.UUID;

public interface EscalateConversationUseCase {

    SupportTicket escalate(UUID ticketId);
}
