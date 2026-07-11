package co.edu.escuelaing.techcup.communications.service.client.rest;

import co.edu.escuelaing.techcup.communications.config.IntegrationProperties;
import co.edu.escuelaing.techcup.communications.exception.IntegrationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestUserServiceClientTest {

    private static final String BASE_URL = "http://user-service";

    private MockRestServiceServer server;
    private RestUserServiceClient client;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new RestUserServiceClient(builder, IntegrationTestProperties.pointingAt(BASE_URL));
    }

    @Test
    void reportsAnExistingUser() {
        server.expect(requestTo(BASE_URL + "/users/" + userId))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withSuccess());

        assertThat(client.exists(userId)).isTrue();
        server.verify();
    }

    @Test
    void reportsAnUnknownUserWithoutFailing() {
        server.expect(requestTo(BASE_URL + "/users/" + userId))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.exists(userId)).isFalse();
        server.verify();
    }

    @Test
    void wrapsADownstreamFailure() {
        server.expect(requestTo(BASE_URL + "/users/" + userId))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.exists(userId)).isInstanceOf(IntegrationException.class);
    }
}
