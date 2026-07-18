package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.domain.exception.ChatClosedException;
import co.edu.escuelaing.techcup.communications.domain.exception.InvalidChatOperationException;
import co.edu.escuelaing.techcup.communications.domain.exception.TeamChatNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.exception.UserNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ChatRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.UserServiceClient;
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
class AddTeamChatParticipantServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private AddTeamChatParticipantService service;

    private final UUID teamId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    void addsParticipantToExistingGroupChat() {
        Chat chat = Chat.create(ChatType.GROUP, teamId);
        when(userServiceClient.exists(userId)).thenReturn(true);
        when(chatRepository.findByTeamIdAndType(teamId, ChatType.GROUP)).thenReturn(Optional.of(chat));
        when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));

        Chat result = service.addParticipant(teamId, userId, ParticipantRole.MEMBER);

        assertThat(result.isParticipant(userId)).isTrue();
        verify(chatRepository).save(chat);
    }

    @Test
    void throwsWhenUserDoesNotExist() {
        when(userServiceClient.exists(userId)).thenReturn(false);

        assertThatThrownBy(() -> service.addParticipant(teamId, userId, ParticipantRole.MEMBER))
                .isInstanceOf(UserNotFoundException.class);
        verify(chatRepository, never()).findByTeamIdAndType(any(), any());
        verify(chatRepository, never()).save(any());
    }

    @Test
    void throwsWhenTeamHasNoGroupChat() {
        when(userServiceClient.exists(userId)).thenReturn(true);
        when(chatRepository.findByTeamIdAndType(teamId, ChatType.GROUP)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addParticipant(teamId, userId, ParticipantRole.MEMBER))
                .isInstanceOf(TeamChatNotFoundException.class);
        verify(chatRepository, never()).save(any());
    }

    @Test
    void throwsWhenChatIsClosed() {
        Chat chat = Chat.create(ChatType.GROUP, teamId);
        chat.close();
        when(userServiceClient.exists(userId)).thenReturn(true);
        when(chatRepository.findByTeamIdAndType(teamId, ChatType.GROUP)).thenReturn(Optional.of(chat));

        assertThatThrownBy(() -> service.addParticipant(teamId, userId, ParticipantRole.MEMBER))
                .isInstanceOf(ChatClosedException.class);
        verify(chatRepository, never()).save(any());
    }

    @Test
    void throwsWhenUserAlreadyParticipant() {
        Chat chat = Chat.create(ChatType.GROUP, teamId);
        chat.addParticipant(userId, ParticipantRole.MEMBER);
        when(userServiceClient.exists(userId)).thenReturn(true);
        when(chatRepository.findByTeamIdAndType(teamId, ChatType.GROUP)).thenReturn(Optional.of(chat));

        assertThatThrownBy(() -> service.addParticipant(teamId, userId, ParticipantRole.MEMBER))
                .isInstanceOf(InvalidChatOperationException.class);
        verify(chatRepository, never()).save(any());
    }
}
