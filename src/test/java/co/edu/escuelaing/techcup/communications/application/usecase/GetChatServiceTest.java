package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.domain.exception.ChatNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.exception.ParticipantNotAllowedException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ChatRepository;
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

    private final UUID caller = UUID.randomUUID();

    @Test
    void returnsChatWhenCallerIsParticipant() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(caller, ParticipantRole.MEMBER);
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));

        assertThat(service.getById(chat.getId(), caller)).isSameAs(chat);
    }

    @Test
    void throwsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(chatRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id, caller))
                .isInstanceOf(ChatNotFoundException.class);
    }

    @Test
    void throwsWhenCallerIsNotAParticipant() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(UUID.randomUUID(), ParticipantRole.MEMBER);
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));

        assertThatThrownBy(() -> service.getById(chat.getId(), caller))
                .isInstanceOf(ParticipantNotAllowedException.class);
    }
}
