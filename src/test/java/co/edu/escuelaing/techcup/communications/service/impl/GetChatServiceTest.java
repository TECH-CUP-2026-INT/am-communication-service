package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.exception.ChatNotFoundException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private GetChatService service;

    @Test
    void returnsChatWhenFound() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));

        assertThat(service.getById(chat.getId())).isSameAs(chat);
    }

    @Test
    void throwsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(chatRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ChatNotFoundException.class);
    }
}
