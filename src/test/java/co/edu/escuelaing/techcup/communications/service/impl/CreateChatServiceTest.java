package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.repository.ChatRepository;
import co.edu.escuelaing.techcup.communications.service.command.CreateChatCommand;
import co.edu.escuelaing.techcup.communications.service.command.ParticipantCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private CreateChatService service;

    private final UUID userA = UUID.randomUUID();
    private final UUID userB = UUID.randomUUID();

    @Test
    void createsChatWithParticipantsAndPersists() {
        CreateChatCommand command = new CreateChatCommand(ChatType.DIRECT, null, List.of(
                new ParticipantCommand(userA, ParticipantRole.MEMBER),
                new ParticipantCommand(userB, ParticipantRole.MEMBER)));
        when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));

        Chat result = service.create(command);

        ArgumentCaptor<Chat> captor = ArgumentCaptor.forClass(Chat.class);
        org.mockito.Mockito.verify(chatRepository).save(captor.capture());
        Chat saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(ChatType.DIRECT);
        assertThat(saved.isParticipant(userA)).isTrue();
        assertThat(saved.isParticipant(userB)).isTrue();
        assertThat(result).isSameAs(saved);
    }

    @Test
    void createsGroupChatWithTeam() {
        UUID team = UUID.randomUUID();
        CreateChatCommand command = new CreateChatCommand(ChatType.GROUP, team, List.of(
                new ParticipantCommand(userA, ParticipantRole.MEMBER)));
        when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));

        Chat result = service.create(command);

        assertThat(result.getType()).isEqualTo(ChatType.GROUP);
        assertThat(result.getTeamId()).isEqualTo(team);
    }
}
