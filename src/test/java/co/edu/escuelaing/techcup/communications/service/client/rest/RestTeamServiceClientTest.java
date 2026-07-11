package co.edu.escuelaing.techcup.communications.service.client.rest;

import co.edu.escuelaing.techcup.communications.exception.IntegrationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestTeamServiceClientTest {

    private static final String BASE_URL = "http://team-service";

    private MockRestServiceServer server;
    private RestTeamServiceClient client;

    private final UUID teamId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new RestTeamServiceClient(builder, IntegrationTestProperties.pointingAt(BASE_URL));
    }

    @Test
    void reportsAnExistingTeam() {
        server.expect(requestTo(BASE_URL + "/teams/" + teamId)).andRespond(withSuccess());

        assertThat(client.exists(teamId)).isTrue();
        server.verify();
    }

    @Test
    void reportsAnUnknownTeamWithoutFailing() {
        server.expect(requestTo(BASE_URL + "/teams/" + teamId)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.exists(teamId)).isFalse();
    }

    @Test
    void wrapsADownstreamFailure() {
        server.expect(requestTo(BASE_URL + "/teams/" + teamId)).andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> client.exists(teamId)).isInstanceOf(IntegrationException.class);
    }
}
