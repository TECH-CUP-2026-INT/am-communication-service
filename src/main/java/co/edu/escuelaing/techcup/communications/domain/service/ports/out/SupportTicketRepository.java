package co.edu.escuelaing.techcup.communications.domain.service.ports.out;

import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;

import java.util.Optional;
import java.util.UUID;

public interface SupportTicketRepository {

    Optional<SupportTicket> findById(UUID id);

    SupportTicket save(SupportTicket ticket);

    boolean isParticipantOfTicketChat(UUID ticketId, UUID userId);
}
