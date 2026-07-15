package co.edu.escuelaing.techcup.communications.domain.exception;

import java.util.UUID;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException(UUID userId) {
        super("No se encontró el usuario: " + userId);
    }
}
