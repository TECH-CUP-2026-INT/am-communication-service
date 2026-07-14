package co.edu.escuelaing.techcup.communications.service.client.feign;

import co.edu.escuelaing.techcup.communications.exception.IntegrationException;
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
class FeignAuditServiceClientTest {

    @Mock
    private AuditServiceFeignClient feignClient;

    private final UUID ticketId = UUID.randomUUID();

    @Test
    void postsTheAuditEventWithItsTimestamp() {
        FeignAuditServiceClient client = new FeignAuditServiceClient(feignClient);

        assertThatCode(() -> client.record("SUPPORT_TRANSITION", ticketId, "ESCALATED: CHATBOT -> AUTOMATIC"))
                .doesNotThrowAnyException();

        ArgumentCaptor<AuditPayload> captor = ArgumentCaptor.forClass(AuditPayload.class);
        verify(feignClient).recordEvent(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo("SUPPORT_TRANSITION");
        assertThat(captor.getValue().entityId()).isEqualTo(ticketId);
        assertThat(captor.getValue().detail()).isEqualTo("ESCALATED: CHATBOT -> AUTOMATIC");
        assertThat(captor.getValue().occurredAt()).isNotNull();
    }

    @Test
    void wrapsADownstreamFailure() {
        doThrow(FeignExceptions.withStatus(500)).when(feignClient).recordEvent(org.mockito.ArgumentMatchers.any());
        FeignAuditServiceClient client = new FeignAuditServiceClient(feignClient);

        assertThatThrownBy(() -> client.record("SUPPORT_TRANSITION", ticketId, "detail"))
                .isInstanceOf(IntegrationException.class);
    }
}
