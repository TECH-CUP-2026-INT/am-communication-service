package co.edu.escuelaing.techcup.communications.exception;

import java.util.UUID;

public class ReportedMessageNotFoundException extends DomainException {

    public ReportedMessageNotFoundException(UUID reportId) {
        super("Reported message not found: " + reportId);
    }
}
