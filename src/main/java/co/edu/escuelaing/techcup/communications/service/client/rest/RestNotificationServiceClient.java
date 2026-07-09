package co.edu.escuelaing.techcup.communications.service.client.rest;

import co.edu.escuelaing.techcup.communications.config.IntegrationProperties;
import co.edu.escuelaing.techcup.communications.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.service.client.NotificationServiceClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class RestNotificationServiceClient implements NotificationServiceClient {

    private static final String SERVICE = "notification service";

    private final RestClient restClient;

    public RestNotificationServiceClient(RestClient.Builder builder, IntegrationProperties properties) {
        this.restClient = builder.baseUrl(properties.notificationService().baseUrl()).build();
    }

    @Override
    public void notify(UUID recipientId, String title, String message) {
        try {
            restClient.post()
                    .uri("/notifications")
                    .body(new NotificationPayload(recipientId, title, message))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new IntegrationException(SERVICE, ex);
        }
    }
}
