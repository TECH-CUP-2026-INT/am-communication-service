package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.dto.SendMessageRequest;
import co.edu.escuelaing.techcup.communications.service.SendMessageUseCase;
import co.edu.escuelaing.techcup.communications.service.command.SendMessageCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatWsControllerTest {

    @Mock
    private SendMessageUseCase sendMessageUseCase;

    @InjectMocks
    private ChatWsController controller;

    @Test
    void delegatesToSendUseCase() {
        UUID chatId = UUID.randomUUID();
        UUID sender = UUID.randomUUID();
        controller.send(new SendMessageRequest(chatId, sender, "hello"));

        ArgumentCaptor<SendMessageCommand> captor = ArgumentCaptor.forClass(SendMessageCommand.class);
        verify(sendMessageUseCase).send(captor.capture());
        assertThat(captor.getValue().chatId()).isEqualTo(chatId);
        assertThat(captor.getValue().senderId()).isEqualTo(sender);
        assertThat(captor.getValue().content()).isEqualTo("hello");
    }
}
