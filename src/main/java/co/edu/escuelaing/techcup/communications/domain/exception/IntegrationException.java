package co.edu.escuelaing.techcup.communications.domain.exception;

/**
 * Raised when a downstream microservice is unreachable or answers unexpectedly.
 * It is answered with 502 and therefore does not extend {@link DomainException}.
 */
public class IntegrationException extends RuntimeException {

    public IntegrationException(String service, Throwable cause) {
        super("El servicio " + service + " no está disponible", cause);
    }
}
