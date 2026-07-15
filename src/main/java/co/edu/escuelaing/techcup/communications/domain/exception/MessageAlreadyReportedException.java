package co.edu.escuelaing.techcup.communications.domain.exception;

import java.util.UUID;

public class MessageAlreadyReportedException extends DomainException {

    public MessageAlreadyReportedException(UUID messageId) {
        super("El mensaje ya fue reportado: " + messageId);
    }
}
