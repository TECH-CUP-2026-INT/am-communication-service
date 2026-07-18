package co.edu.escuelaing.techcup.communications.domain.exception;

import java.util.UUID;

public class TeamChatNotFoundException extends DomainException {

    public TeamChatNotFoundException(UUID teamId) {
        super("No se encontró un chat de tipo GROUP para el equipo: " + teamId);
    }
}
