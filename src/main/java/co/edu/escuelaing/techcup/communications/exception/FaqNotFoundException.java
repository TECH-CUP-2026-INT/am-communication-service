package co.edu.escuelaing.techcup.communications.exception;

import java.util.UUID;

public class FaqNotFoundException extends DomainException {

    public FaqNotFoundException(UUID faqId) {
        super("FAQ not found: " + faqId);
    }
}
