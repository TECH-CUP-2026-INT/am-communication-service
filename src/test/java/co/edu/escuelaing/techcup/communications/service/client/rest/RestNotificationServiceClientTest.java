package co.edu.escuelaing.techcup.communications.service.client.rest;

import co.edu.escuelaing.techcup.communications.exception.IntegrationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestNotificationServiceClientTest {

    private static final String BASE_URL = "http://notification-service";

    private MockRestServiceServer server;
    private RestNotificationServiceClient client;

    private final UUID recipient = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new RestNotificationServiceClient(builder, IntegrationTestProperties.pointingAt(BASE_URL));
    }

    @Test
    void postsTheNotificationPayload() {
        server.expect(requestTo(BASE_URL + "/notifications"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.recipientId").value(recipient.toString()))
                .andExpect(jsonPath("$.title").value("Support ticket updated"))
                .andExpect(jsonPath("$.message").value("ESCALATED: CHATBOT -> AUTOMATIC"))
                .andRespond(withSuccess());

        assertThatCode(() -> client.notify(recipient, "Support ticket updated", "ESCALATED: CHATBOT -> AUTOMATIC"))
                .doesNotThrowAnyException();
        server.verify();
    }

    @Test
    void wrapsADownstreamFailure() {
        server.expect(requestTo(BASE_URL + "/notifications"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> client.notify(recipient, "title", "message"))
                .isInstanceOf(IntegrationException.class);
    }
}
