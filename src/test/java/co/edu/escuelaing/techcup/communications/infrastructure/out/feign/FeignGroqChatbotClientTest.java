package co.edu.escuelaing.techcup.communications.infrastructure.out.feign;

import co.edu.escuelaing.techcup.communications.infrastructure.config.GroqProperties;
import co.edu.escuelaing.techcup.communications.domain.exception.IntegrationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeignGroqChatbotClientTest {

    @Mock
    private GroqChatFeignClient feignClient;

    private FeignGroqChatbotClient client() {
        return new FeignGroqChatbotClient(
                feignClient, new GroqProperties("http://groq", "test-key", "test-model", "You are a test assistant."));
    }

    @Test
    void returnsTheGeneratedReply() {
        GroqChatResponse response = new GroqChatResponse(
                List.of(new GroqChatChoice(new GroqChatMessage("assistant", "Try resetting your password."))));
        when(feignClient.createChatCompletion(any())).thenReturn(response);

        assertThat(client().generateReply("I can't log in")).isEqualTo("Try resetting your password.");
    }

    @Test
    void sendsTheSystemPromptAndModel() {
        GroqChatResponse response = new GroqChatResponse(
                List.of(new GroqChatChoice(new GroqChatMessage("assistant", "ok"))));
        when(feignClient.createChatCompletion(any())).thenReturn(response);

        client().generateReply("I can't log in");

        ArgumentCaptor<GroqChatRequest> captor = ArgumentCaptor.forClass(GroqChatRequest.class);
        verify(feignClient).createChatCompletion(captor.capture());
        assertThat(captor.getValue().model()).isEqualTo("test-model");
        assertThat(captor.getValue().messages())
                .extracting(GroqChatMessage::role, GroqChatMessage::content)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("system", "You are a test assistant."),
                        org.assertj.core.groups.Tuple.tuple("user", "I can't log in"));
    }

    @Test
    void wrapsADownstreamFailure() {
        when(feignClient.createChatCompletion(any())).thenThrow(FeignExceptions.withStatus(500));

        var chatbot = client();
        assertThatThrownBy(() -> chatbot.generateReply("hi")).isInstanceOf(IntegrationException.class);
    }

    @Test
    void wrapsAnEmptyChoicesResponse() {
        when(feignClient.createChatCompletion(any())).thenReturn(new GroqChatResponse(List.of()));

        var chatbot = client();
        assertThatThrownBy(() -> chatbot.generateReply("hi")).isInstanceOf(IntegrationException.class);
    }
}
