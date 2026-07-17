package co.edu.escuelaing.techcup.communications.infrastructure.out.feign;

import co.edu.escuelaing.techcup.communications.domain.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.NotificationServiceClient;
import co.edu.escuelaing.techcup.communications.domain.service.support.SupportBotIdentity;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class FeignNotificationServiceClient implements NotificationServiceClient {

    private static final String SERVICE = "notification service";

    // Our domain has no display-name concept anywhere (everything is UUID-keyed); this is the
    // only sender this port is used for today (support-ticket transitions), so a fixed name is
    // fine. Revisit if this port ever fronts a real, named human message.
    private static final String SENDER_NAME = "TechCup Support";

    private final NotificationServiceFeignClient feignClient;

    public FeignNotificationServiceClient(NotificationServiceFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    @Override
    public void notify(UUID chatId, UUID recipientId, String messagePreview) {
        try {
            feignClient.sendNotification(new ChatMessageEvent(
                    chatId, SupportBotIdentity.BOT_USER_ID, SENDER_NAME, recipientId, messagePreview, Instant.now()));
        } catch (FeignException ex) {
            throw new IntegrationException(SERVICE, ex);
        }
    }
}
