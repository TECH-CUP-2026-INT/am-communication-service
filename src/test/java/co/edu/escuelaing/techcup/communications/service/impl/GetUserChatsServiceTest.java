package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.repository.ChatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserChatsServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private GetUserChatsService service;

    @Test
    void returnsChatsForUser() {
        UUID userId = UUID.randomUUID();
        List<Chat> chats = List.of(Chat.create(ChatType.DIRECT, null), Chat.create(ChatType.SUPPORT, null));
        when(chatRepository.findAllByParticipantUserId(userId)).thenReturn(chats);

        assertThat(service.getByUser(userId)).isEqualTo(chats);
    }

    @Test
    void returnsEmptyWhenUserHasNoChats() {
        UUID userId = UUID.randomUUID();
        when(chatRepository.findAllByParticipantUserId(userId)).thenReturn(List.of());

        assertThat(service.getByUser(userId)).isEmpty();
    }
}
