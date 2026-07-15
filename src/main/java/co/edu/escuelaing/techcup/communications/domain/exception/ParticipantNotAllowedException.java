package co.edu.escuelaing.techcup.communications.domain.exception;

import java.util.UUID;

public class ParticipantNotAllowedException extends DomainException {

    public ParticipantNotAllowedException(UUID userId, UUID chatId) {
        super("El usuario " + userId + " no tiene permitido participar en el chat " + chatId);
    }
}
