package co.edu.escuelaing.techcup.communications.domain.exception;

import java.util.UUID;

public class TeamNotFoundException extends DomainException {

    public TeamNotFoundException(UUID teamId) {
        super("No se encontró el equipo: " + teamId);
    }
}
