package co.edu.escuelaing.techcup.communications.domain.exception;

import java.util.UUID;

public class ChatNotFoundException extends DomainException {

    public ChatNotFoundException(UUID chatId) {
        super("No se encontró el chat: " + chatId);
    }
}
