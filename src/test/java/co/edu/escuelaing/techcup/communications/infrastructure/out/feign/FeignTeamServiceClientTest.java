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
class FeignTeamServiceClientTest {

    private static final String BASE_URL = "http://team-service";

    @Mock
    private TeamServiceFeignClient feignClient;

    private final UUID teamId = UUID.randomUUID();

    @Test
    void reportsAnExistingTeam() {
        FeignTeamServiceClient client = new FeignTeamServiceClient(
                feignClient, IntegrationTestProperties.pointingAt(BASE_URL));

        assertThat(client.exists(teamId)).isTrue();
    }

    @Test
    void reportsAnUnknownTeamWithoutFailing() {
        doThrow(FeignExceptions.withStatus(404)).when(feignClient).getTeam(teamId);
        FeignTeamServiceClient client = new FeignTeamServiceClient(
                feignClient, IntegrationTestProperties.pointingAt(BASE_URL));

        assertThat(client.exists(teamId)).isFalse();
    }

    @Test
    void wrapsADownstreamFailure() {
        doThrow(FeignExceptions.withStatus(500)).when(feignClient).getTeam(teamId);
        FeignTeamServiceClient client = new FeignTeamServiceClient(
                feignClient, IntegrationTestProperties.pointingAt(BASE_URL));

        assertThatThrownBy(() -> client.exists(teamId)).isInstanceOf(IntegrationException.class);
    }

    @Test
    void treatsEveryTeamAsExistingWhenTheCheckIsDisabled() {
        FeignTeamServiceClient client = new FeignTeamServiceClient(
                feignClient, IntegrationTestProperties.pointingAt(BASE_URL, false));

        assertThat(client.exists(teamId)).isTrue();
        verifyNoInteractions(feignClient);
    }
}
