package co.edu.escuelaing.techcup.communications.service.client.feign;

import co.edu.escuelaing.techcup.communications.config.GroqProperties;
import co.edu.escuelaing.techcup.communications.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.service.client.ChatbotClient;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Calls Groq's OpenAI-compatible chat completion API. The API key is only ever used to build
 * the {@code Authorization} header (see {@link GroqFeignClientConfig}) — it is never logged or
 * included in any exception message.
 */
@Component
public class FeignGroqChatbotClient implements ChatbotClient {

    private static final String SERVICE = "groq chatbot service";

    private final GroqChatFeignClient feignClient;
    private final String model;
    private final String systemPrompt;

    public FeignGroqChatbotClient(GroqChatFeignClient feignClient, GroqProperties properties) {
        this.feignClient = feignClient;
        this.model = properties.model();
        this.systemPrompt = properties.systemPrompt();
    }

    @Override
    public String generateReply(String userMessage) {
        try {
            GroqChatRequest request = new GroqChatRequest(model, List.of(
                    new GroqChatMessage("system", systemPrompt),
                    new GroqChatMessage("user", userMessage)));
            GroqChatResponse response = feignClient.createChatCompletion(request);
            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new IntegrationException(SERVICE, new IllegalStateException("empty response"));
            }
            return response.choices().get(0).message().content();
        } catch (FeignException ex) {
            throw new IntegrationException(SERVICE, ex);
        }
    }
}
