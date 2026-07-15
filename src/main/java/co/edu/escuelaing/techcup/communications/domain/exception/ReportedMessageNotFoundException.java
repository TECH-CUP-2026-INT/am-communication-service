package co.edu.escuelaing.techcup.communications.domain.exception;

import java.util.UUID;

public class ReportedMessageNotFoundException extends DomainException {

    public ReportedMessageNotFoundException(UUID reportId) {
        super("No se encontró el reporte de mensaje: " + reportId);
    }
}
