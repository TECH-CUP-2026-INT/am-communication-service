package co.edu.escuelaing.techcup.communications.domain.exception;

import java.util.UUID;

public class MessageNotFoundException extends DomainException {

    public MessageNotFoundException(UUID messageId) {
        super("No se encontró el mensaje: " + messageId);
    }
}
