package co.edu.escuelaing.techcup.communications.exception;

import java.util.UUID;

public class SupportTicketNotFoundException extends DomainException {

    public SupportTicketNotFoundException(UUID ticketId) {
        super("Support ticket not found: " + ticketId);
    }
}
