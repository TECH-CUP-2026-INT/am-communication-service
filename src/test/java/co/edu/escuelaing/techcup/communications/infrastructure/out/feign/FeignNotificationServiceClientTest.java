package co.edu.escuelaing.techcup.communications.infrastructure.out.feign;

import co.edu.escuelaing.techcup.communications.domain.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.domain.service.support.SupportBotIdentity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FeignNotificationServiceClientTest {

    @Mock
    private NotificationServiceFeignClient feignClient;

    private final UUID chatId = UUID.randomUUID();
    private final UUID recipientId = UUID.randomUUID();

    @Test
    void sendsAChatMessageEventShapedNotification() {
        FeignNotificationServiceClient client = new FeignNotificationServiceClient(feignClient);

        assertThatCode(() -> client.notify(chatId, recipientId, "detail"))
                .doesNotThrowAnyException();

        ArgumentCaptor<ChatMessageEvent> captor = ArgumentCaptor.forClass(ChatMessageEvent.class);
        verify(feignClient).sendNotification(captor.capture());
        assertThat(captor.getValue().chatId()).isEqualTo(chatId);
        assertThat(captor.getValue().recipientId()).isEqualTo(recipientId);
        assertThat(captor.getValue().messagePreview()).isEqualTo("detail");
        assertThat(captor.getValue().senderId()).isEqualTo(SupportBotIdentity.BOT_USER_ID);
        assertThat(captor.getValue().senderName()).isNotBlank();
        assertThat(captor.getValue().sentAt()).isNotNull();
    }

    @Test
    void wrapsADownstreamFailure() {
        doThrow(FeignExceptions.withStatus(500)).when(feignClient).sendNotification(org.mockito.ArgumentMatchers.any());
        FeignNotificationServiceClient client = new FeignNotificationServiceClient(feignClient);

        assertThatThrownBy(() -> client.notify(chatId, recipientId, "message"))
                .isInstanceOf(IntegrationException.class);
    }
}
