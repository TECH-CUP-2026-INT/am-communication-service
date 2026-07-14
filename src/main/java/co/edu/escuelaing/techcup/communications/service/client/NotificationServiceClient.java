package co.edu.escuelaing.techcup.communications.service.client;

import java.util.UUID;

/** Outbound port to the Notifications microservice. */
public interface NotificationServiceClient {

    void notify(UUID recipientId, String title, String message);
}
