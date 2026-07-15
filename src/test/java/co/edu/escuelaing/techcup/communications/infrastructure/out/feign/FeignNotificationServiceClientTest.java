package co.edu.escuelaing.techcup.communications.infrastructure.out.feign;

import co.edu.escuelaing.techcup.communications.domain.exception.IntegrationException;
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

    private final UUID recipientId = UUID.randomUUID();

    @Test
    void sendsTheNotificationPayload() {
        FeignNotificationServiceClient client = new FeignNotificationServiceClient(feignClient);

        assertThatCode(() -> client.notify(recipientId, "Support ticket updated", "detail"))
                .doesNotThrowAnyException();

        ArgumentCaptor<NotificationPayload> captor = ArgumentCaptor.forClass(NotificationPayload.class);
        verify(feignClient).sendNotification(captor.capture());
        assertThat(captor.getValue().recipientId()).isEqualTo(recipientId);
        assertThat(captor.getValue().title()).isEqualTo("Support ticket updated");
        assertThat(captor.getValue().message()).isEqualTo("detail");
    }

    @Test
    void wrapsADownstreamFailure() {
        doThrow(FeignExceptions.withStatus(500)).when(feignClient).sendNotification(org.mockito.ArgumentMatchers.any());
        FeignNotificationServiceClient client = new FeignNotificationServiceClient(feignClient);

        assertThatThrownBy(() -> client.notify(recipientId, "title", "message"))
                .isInstanceOf(IntegrationException.class);
    }
}
