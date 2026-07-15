package co.edu.escuelaing.techcup.communications.domain.exception;

import java.util.UUID;

public class ChatClosedException extends DomainException {

    public ChatClosedException(UUID chatId) {
        super("El chat está cerrado: " + chatId);
    }
}
