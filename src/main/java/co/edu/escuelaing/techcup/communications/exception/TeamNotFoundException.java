package co.edu.escuelaing.techcup.communications.exception;

import java.util.UUID;

public class TeamNotFoundException extends DomainException {

    public TeamNotFoundException(UUID teamId) {
        super("Team not found: " + teamId);
    }
}
