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

class RestAuditServiceClientTest {

    private static final String BASE_URL = "http://audit-service";

    private MockRestServiceServer server;
    private RestAuditServiceClient client;

    private final UUID ticketId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new RestAuditServiceClient(builder, IntegrationTestProperties.pointingAt(BASE_URL));
    }

    @Test
    void postsTheAuditEventWithItsTimestamp() {
        server.expect(requestTo(BASE_URL + "/audit-events"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.eventType").value("SUPPORT_TRANSITION"))
                .andExpect(jsonPath("$.entityId").value(ticketId.toString()))
                .andExpect(jsonPath("$.detail").value("ESCALATED: CHATBOT -> AUTOMATIC"))
                .andExpect(jsonPath("$.occurredAt").exists())
                .andRespond(withSuccess());

        assertThatCode(() -> client.record("SUPPORT_TRANSITION", ticketId, "ESCALATED: CHATBOT -> AUTOMATIC"))
                .doesNotThrowAnyException();
        server.verify();
    }

    @Test
    void wrapsADownstreamFailure() {
        server.expect(requestTo(BASE_URL + "/audit-events"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.record("SUPPORT_TRANSITION", ticketId, "detail"))
                .isInstanceOf(IntegrationException.class);
    }
}
