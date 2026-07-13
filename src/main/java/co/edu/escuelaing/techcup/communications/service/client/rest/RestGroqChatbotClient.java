package co.edu.escuelaing.techcup.communications.service.client.rest;

import co.edu.escuelaing.techcup.communications.config.GroqProperties;
import co.edu.escuelaing.techcup.communications.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.service.client.ChatbotClient;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Calls Groq's OpenAI-compatible chat completion API. The API key is only ever used to build
 * the {@code Authorization} header at construction time — it is never logged or included in any
 * exception message.
 */
@Component
public class RestGroqChatbotClient implements ChatbotClient {

    private static final String SERVICE = "groq chatbot service";

    private final RestClient restClient;
    private final String model;
    private final String systemPrompt;

    public RestGroqChatbotClient(RestClient.Builder builder, GroqProperties properties) {
        this.restClient = builder.baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .build();
        this.model = properties.model();
        this.systemPrompt = properties.systemPrompt();
    }

    @Override
    public String generateReply(String userMessage) {
        try {
            GroqChatRequest request = new GroqChatRequest(model, List.of(
                    new GroqChatMessage("system", systemPrompt),
                    new GroqChatMessage("user", userMessage)));
            GroqChatResponse response = restClient.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(GroqChatResponse.class);
            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new IntegrationException(SERVICE, new IllegalStateException("empty response"));
            }
            return response.choices().get(0).message().content();
        } catch (RestClientException ex) {
            throw new IntegrationException(SERVICE, ex);
        }
    }
}
