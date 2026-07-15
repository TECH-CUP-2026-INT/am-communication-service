package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.domain.exception.ChatNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.exception.InvalidChatOperationException;
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

    private final UUID caller = UUID.randomUUID();

    @Test
    void closesOpenChatWhenCallerIsParticipant() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(caller, ParticipantRole.MEMBER);
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));
        when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));

        Chat result = service.close(chat.getId(), caller);

        assertThat(result.isClosed()).isTrue();
        verify(chatRepository).save(chat);
    }

    @Test
    void throwsWhenChatNotFound() {
        UUID id = UUID.randomUUID();
        when(chatRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.close(id, caller)).isInstanceOf(ChatNotFoundException.class);
        verify(chatRepository, never()).save(any());
    }

    @Test
    void rejectsClosingAlreadyClosedChat() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(caller, ParticipantRole.MEMBER);
        chat.close();
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));

        assertThatThrownBy(() -> service.close(chat.getId(), caller))
                .isInstanceOf(InvalidChatOperationException.class);
        verify(chatRepository, never()).save(any());
    }

    @Test
    void throwsWhenCallerIsNotAParticipant() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(UUID.randomUUID(), ParticipantRole.MEMBER);
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));

        assertThatThrownBy(() -> service.close(chat.getId(), caller))
                .isInstanceOf(ParticipantNotAllowedException.class);
        verify(chatRepository, never()).save(any());
    }
}
