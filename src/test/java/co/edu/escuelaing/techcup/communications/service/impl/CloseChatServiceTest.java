package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.exception.ChatNotFoundException;
import co.edu.escuelaing.techcup.communications.exception.InvalidChatOperationException;
import co.edu.escuelaing.techcup.communications.repository.ChatRepository;
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
class CloseChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private CloseChatService service;

    @Test
    void closesOpenChat() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));
        when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));

        Chat result = service.close(chat.getId());

        assertThat(result.isClosed()).isTrue();
        verify(chatRepository).save(chat);
    }

    @Test
    void throwsWhenChatNotFound() {
        UUID id = UUID.randomUUID();
        when(chatRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.close(id)).isInstanceOf(ChatNotFoundException.class);
        verify(chatRepository, never()).save(any());
    }

    @Test
    void rejectsClosingAlreadyClosedChat() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.close();
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));

        assertThatThrownBy(() -> service.close(chat.getId()))
                .isInstanceOf(InvalidChatOperationException.class);
        verify(chatRepository, never()).save(any());
    }
}
