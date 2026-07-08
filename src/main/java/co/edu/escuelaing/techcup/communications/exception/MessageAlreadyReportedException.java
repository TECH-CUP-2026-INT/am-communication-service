package co.edu.escuelaing.techcup.communications.exception;

import java.util.UUID;

public class MessageAlreadyReportedException extends DomainException {

    public MessageAlreadyReportedException(UUID messageId) {
        super("Message already reported: " + messageId);
    }
}
