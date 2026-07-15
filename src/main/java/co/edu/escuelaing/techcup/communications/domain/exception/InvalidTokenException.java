package co.edu.escuelaing.techcup.communications.domain.exception;

/**
 * Raised when a bearer token is absent, malformed, expired or signed with an unexpected key.
 * It is answered with 401 and therefore does not extend {@link DomainException}.
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
