package co.edu.escuelaing.techcup.communications.exception;

import java.util.UUID;

public class ChatClosedException extends DomainException {

    public ChatClosedException(UUID chatId) {
        super("Chat is closed: " + chatId);
    }
}
