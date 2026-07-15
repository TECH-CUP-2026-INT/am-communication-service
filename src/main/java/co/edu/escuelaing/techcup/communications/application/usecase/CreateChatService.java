package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.exception.TeamNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.exception.UserNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ChatRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.CreateChatUseCase;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.TeamServiceClient;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.UserServiceClient;
import co.edu.escuelaing.techcup.communications.application.usecase.command.CreateChatCommand;
import co.edu.escuelaing.techcup.communications.application.usecase.command.ParticipantCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateChatService implements CreateChatUseCase {

    private final ChatRepository chatRepository;
    private final UserServiceClient userServiceClient;
    private final TeamServiceClient teamServiceClient;

    @Override
    @Transactional
    public Chat create(CreateChatCommand command) {
        command.participants().stream().map(ParticipantCommand::userId).forEach(this::requireExistingUser);
        requireExistingTeam(command.teamId());

        Chat chat = Chat.create(command.type(), command.teamId());
        command.participants().forEach(p -> chat.addParticipant(p.userId(), p.role()));
        return chatRepository.save(chat);
    }

    private void requireExistingUser(UUID userId) {
        if (!userServiceClient.exists(userId)) {
            throw new UserNotFoundException(userId);
        }
    }

    private void requireExistingTeam(UUID teamId) {
        if (teamId != null && !teamServiceClient.exists(teamId)) {
            throw new TeamNotFoundException(teamId);
        }
    }
}
