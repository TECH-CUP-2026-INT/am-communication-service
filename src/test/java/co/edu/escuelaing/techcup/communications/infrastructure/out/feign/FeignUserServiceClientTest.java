package co.edu.escuelaing.techcup.communications.infrastructure.out.feign;

import co.edu.escuelaing.techcup.communications.domain.exception.IntegrationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class FeignUserServiceClientTest {

    private static final String BASE_URL = "http://user-service";

    @Mock
    private UserServiceFeignClient feignClient;

    private final UUID userId = UUID.randomUUID();

    @Test
    void reportsAnExistingUser() {
        FeignUserServiceClient client = new FeignUserServiceClient(
                feignClient, IntegrationTestProperties.pointingAt(BASE_URL));

        assertThat(client.exists(userId)).isTrue();
    }

    @Test
    void reportsAnUnknownUserWithoutFailing() {
        doThrow(FeignExceptions.withStatus(404)).when(feignClient).getUser(userId);
        FeignUserServiceClient client = new FeignUserServiceClient(
                feignClient, IntegrationTestProperties.pointingAt(BASE_URL));

        assertThat(client.exists(userId)).isFalse();
    }

    @Test
    void wrapsADownstreamFailure() {
        doThrow(FeignExceptions.withStatus(500)).when(feignClient).getUser(userId);
        FeignUserServiceClient client = new FeignUserServiceClient(
                feignClient, IntegrationTestProperties.pointingAt(BASE_URL));

        assertThatThrownBy(() -> client.exists(userId)).isInstanceOf(IntegrationException.class);
    }

    @Test
    void treatsEveryUserAsExistingWhenTheCheckIsDisabled() {
        FeignUserServiceClient client = new FeignUserServiceClient(
                feignClient, IntegrationTestProperties.pointingAt(BASE_URL, false));

        assertThat(client.exists(userId)).isTrue();
        verifyNoInteractions(feignClient);
    }
}
