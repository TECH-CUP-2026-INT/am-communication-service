package co.edu.escuelaing.techcup.communications.exception;

import java.util.UUID;

public class SubscriptionNotAllowedException extends DomainException {

    public SubscriptionNotAllowedException(String destination) {
        super("Subscription to " + destination + " is not allowed");
    }

    public SubscriptionNotAllowedException(UUID userId, String destination) {
        super("User " + userId + " is not allowed to subscribe to " + destination);
    }
}
