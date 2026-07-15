package co.edu.escuelaing.techcup.communications.domain.exception;

import java.util.UUID;

public class SupportTicketNotFoundException extends DomainException {

    public SupportTicketNotFoundException(UUID ticketId) {
        super("No se encontró el ticket de soporte: " + ticketId);
    }
}
