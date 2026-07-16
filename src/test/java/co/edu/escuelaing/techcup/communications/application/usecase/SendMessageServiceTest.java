package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.Message;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.domain.exception.ChatClosedException;
import co.edu.escuelaing.techcup.communications.domain.exception.ChatNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.exception.ParticipantNotAllowedException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ChatRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.MessageRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.MessagePublisher;
import co.edu.escuelaing.techcup.communications.application.usecase.command.SendMessageCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendMessageServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MessagePublisher messagePublisher;

    @InjectMocks
    private SendMessageService service;

    private final UUID sender = UUID.randomUUID();

    private Chat openChatWithSender() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(sender, ParticipantRole.MEMBER);
        return chat;
    }

    @Test
    void persistsMessageFromParticipant() {
        Chat chat = openChatWithSender();
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        Message message = service.send(new SendMessageCommand(chat.getId(), sender, "hello"));

        assertThat(message.getContent()).isEqualTo("hello");
        assertThat(message.getSenderId()).isEqualTo(sender);
        verify(messageRepository).save(message);
        verify(messagePublisher).publishChatMessage(message);
    }

    @Test
    void throwsWhenChatNotFound() {
        UUID chatId = UUID.randomUUID();
        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        SendMessageCommand command = new SendMessageCommand(chatId, sender, "hi");
        assertThatThrownBy(() -> service.send(command))
                .isInstanceOf(ChatNotFoundException.class);
        verify(messageRepository, never()).save(any());
    }

    @Test
    void rejectsMessageOnClosedChat() {
        Chat chat = openChatWithSender();
        chat.close();
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));

        SendMessageCommand command = new SendMessageCommand(chat.getId(), sender, "hi");
        assertThatThrownBy(() -> service.send(command))
                .isInstanceOf(ChatClosedException.class);
        verify(messageRepository, never()).save(any());
    }

    @Test
    void rejectsMessageFromNonParticipant() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));

        SendMessageCommand command = new SendMessageCommand(chat.getId(), sender, "hi");
        assertThatThrownBy(() -> service.send(command))
                .isInstanceOf(ParticipantNotAllowedException.class);
        verify(messageRepository, never()).save(any());
    }
}
