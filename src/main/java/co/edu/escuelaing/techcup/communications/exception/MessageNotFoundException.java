package co.edu.escuelaing.techcup.communications.exception;

import java.util.UUID;

public class MessageNotFoundException extends DomainException {

    public MessageNotFoundException(UUID messageId) {
        super("Message not found: " + messageId);
    }
}
