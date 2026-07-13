package co.edu.escuelaing.techcup.communications.service.client.rest;

import co.edu.escuelaing.techcup.communications.config.GroqProperties;
import co.edu.escuelaing.techcup.communications.exception.IntegrationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestGroqChatbotClientTest {

    private static final String BASE_URL = "http://groq";

    private MockRestServiceServer server;
    private RestGroqChatbotClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new RestGroqChatbotClient(builder, new GroqProperties(BASE_URL, "test-key", "test-model", "You are a test assistant."));
    }

    @Test
    void returnsTheGeneratedReply() {
        server.expect(requestTo(BASE_URL + "/chat/completions"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("You are a test assistant.")))
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"role":"assistant","content":"Try resetting your password."}}]}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.generateReply("I can't log in")).isEqualTo("Try resetting your password.");
        server.verify();
    }

    @Test
    void wrapsADownstreamFailure() {
        server.expect(requestTo(BASE_URL + "/chat/completions"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.generateReply("hi")).isInstanceOf(IntegrationException.class);
    }

    @Test
    void wrapsAnEmptyChoicesResponse() {
        server.expect(requestTo(BASE_URL + "/chat/completions"))
                .andRespond(withSuccess("""
                        {"choices":[]}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.generateReply("hi")).isInstanceOf(IntegrationException.class);
    }
}
