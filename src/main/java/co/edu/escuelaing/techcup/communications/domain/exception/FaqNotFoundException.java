package co.edu.escuelaing.techcup.communications.domain.exception;

import java.util.UUID;

public class FaqNotFoundException extends DomainException {

    public FaqNotFoundException(UUID faqId) {
        super("No se encontró la pregunta frecuente: " + faqId);
    }
}
