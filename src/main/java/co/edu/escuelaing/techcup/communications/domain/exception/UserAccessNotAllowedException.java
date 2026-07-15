package co.edu.escuelaing.techcup.communications.domain.exception;

import java.util.UUID;

public class UserAccessNotAllowedException extends DomainException {

    public UserAccessNotAllowedException(UUID callerId, UUID targetUserId) {
        super("El usuario " + callerId + " no tiene permitido ver los chats del usuario " + targetUserId);
    }
}
