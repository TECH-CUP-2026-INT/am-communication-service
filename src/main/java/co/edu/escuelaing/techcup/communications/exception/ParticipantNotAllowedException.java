package co.edu.escuelaing.techcup.communications.exception;

import java.util.UUID;

public class ParticipantNotAllowedException extends DomainException {

    public ParticipantNotAllowedException(UUID userId, UUID chatId) {
        super("User " + userId + " is not allowed to participate in chat " + chatId);
    }
}
