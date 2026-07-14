package co.edu.escuelaing.techcup.communications.service.client.feign;

import co.edu.escuelaing.techcup.communications.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.service.client.NotificationServiceClient;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FeignNotificationServiceClient implements NotificationServiceClient {

    private static final String SERVICE = "notification service";

    private final NotificationServiceFeignClient feignClient;

    public FeignNotificationServiceClient(NotificationServiceFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    @Override
    public void notify(UUID recipientId, String title, String message) {
        try {
            feignClient.sendNotification(new NotificationPayload(recipientId, title, message));
        } catch (FeignException ex) {
            throw new IntegrationException(SERVICE, ex);
        }
    }
}
