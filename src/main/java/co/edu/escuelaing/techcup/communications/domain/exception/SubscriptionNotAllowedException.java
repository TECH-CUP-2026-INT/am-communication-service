package co.edu.escuelaing.techcup.communications.domain.exception;

import java.util.UUID;

public class SubscriptionNotAllowedException extends DomainException {

    public SubscriptionNotAllowedException(String destination) {
        super("La suscripción a " + destination + " no está permitida");
    }

    public SubscriptionNotAllowedException(UUID userId, String destination) {
        super("El usuario " + userId + " no tiene permitido suscribirse a " + destination);
    }
}
