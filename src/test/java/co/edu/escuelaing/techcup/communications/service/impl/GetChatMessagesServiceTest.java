package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.exception.ChatNotFoundException;
import co.edu.escuelaing.techcup.communications.repository.ChatRepository;
import co.edu.escuelaing.techcup.communications.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetChatMessagesServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private GetChatMessagesService service;

    private final UUID chatId = UUID.randomUUID();
    private final Pageable pageable = PageRequest.of(0, 20);

    @Test
    void returnsPageWhenChatExists() {
        Page<Message> page = new PageImpl<>(List.of());
        when(chatRepository.existsById(chatId)).thenReturn(true);
        when(messageRepository.findByChat_Id(chatId, pageable)).thenReturn(page);

        assertThat(service.getByChat(chatId, pageable)).isSameAs(page);
    }

    @Test
    void throwsWhenChatDoesNotExist() {
        when(chatRepository.existsById(chatId)).thenReturn(false);

        assertThatThrownBy(() -> service.getByChat(chatId, pageable))
                .isInstanceOf(ChatNotFoundException.class);
        verify(messageRepository, never()).findByChat_Id(chatId, pageable);
    }
}
