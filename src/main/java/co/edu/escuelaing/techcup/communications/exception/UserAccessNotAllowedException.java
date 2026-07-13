package co.edu.escuelaing.techcup.communications.exception;

import java.util.UUID;

public class UserAccessNotAllowedException extends DomainException {

    public UserAccessNotAllowedException(UUID callerId, UUID targetUserId) {
        super("User " + callerId + " is not allowed to view chats of user " + targetUserId);
    }
}
