package co.edu.escuelaing.techcup.communications.exception;

/**
 * Base type for every domain-level error. Concrete subtypes are mapped to HTTP
 * status codes by the GlobalExceptionHandler.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
