package co.edu.escuelaing.techcup.communications.exception;

import java.util.UUID;

public class ChatNotFoundException extends DomainException {

    public ChatNotFoundException(UUID chatId) {
        super("Chat not found: " + chatId);
    }
}
