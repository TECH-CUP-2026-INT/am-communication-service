package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.domain.exception.TeamNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.exception.UserNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ChatRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.TeamServiceClient;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.UserServiceClient;
import co.edu.escuelaing.techcup.communications.application.usecase.command.CreateChatCommand;
import co.edu.escuelaing.techcup.communications.application.usecase.command.ParticipantCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private TeamServiceClient teamServiceClient;

    @InjectMocks
    private CreateChatService service;

    private final UUID userA = UUID.randomUUID();
    private final UUID userB = UUID.randomUUID();

    private void allUsersExist() {
        lenient().when(userServiceClient.exists(any())).thenReturn(true);
    }

    @Test
    void createsChatWithParticipantsAndPersists() {
        allUsersExist();
        CreateChatCommand command = new CreateChatCommand(ChatType.DIRECT, null, List.of(
                new ParticipantCommand(userA, ParticipantRole.MEMBER),
                new ParticipantCommand(userB, ParticipantRole.MEMBER)));
        when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));

        Chat result = service.create(command);

        ArgumentCaptor<Chat> captor = ArgumentCaptor.forClass(Chat.class);
        verify(chatRepository).save(captor.capture());
        Chat saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(ChatType.DIRECT);
        assertThat(saved.isParticipant(userA)).isTrue();
        assertThat(saved.isParticipant(userB)).isTrue();
        assertThat(result).isSameAs(saved);
    }

    @Test
    void createsGroupChatWithTeam() {
        allUsersExist();
        UUID team = UUID.randomUUID();
        when(teamServiceClient.exists(team)).thenReturn(true);
        CreateChatCommand command = new CreateChatCommand(ChatType.GROUP, team, List.of(
                new ParticipantCommand(userA, ParticipantRole.MEMBER)));
        when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));

        Chat result = service.create(command);

        assertThat(result.getType()).isEqualTo(ChatType.GROUP);
        assertThat(result.getTeamId()).isEqualTo(team);
    }

    @Test
    void rejectsAParticipantUnknownToTheUserService() {
        when(userServiceClient.exists(userA)).thenReturn(false);
        CreateChatCommand command = new CreateChatCommand(ChatType.DIRECT, null, List.of(
                new ParticipantCommand(userA, ParticipantRole.MEMBER)));

        assertThatThrownBy(() -> service.create(command)).isInstanceOf(UserNotFoundException.class);
        verify(chatRepository, never()).save(any());
    }

    @Test
    void rejectsAGroupChatWhoseTeamIsUnknownToTheTeamService() {
        allUsersExist();
        UUID team = UUID.randomUUID();
        when(teamServiceClient.exists(team)).thenReturn(false);
        CreateChatCommand command = new CreateChatCommand(ChatType.GROUP, team, List.of(
                new ParticipantCommand(userA, ParticipantRole.MEMBER)));

        assertThatThrownBy(() -> service.create(command)).isInstanceOf(TeamNotFoundException.class);
        verify(chatRepository, never()).save(any());
    }

    @Test
    void doesNotCallTheTeamServiceForChatsWithoutATeam() {
        allUsersExist();
        when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(new CreateChatCommand(ChatType.DIRECT, null, List.of(
                new ParticipantCommand(userA, ParticipantRole.MEMBER))));

        verify(teamServiceClient, never()).exists(any());
    }
}
