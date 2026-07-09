package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.exception.TeamNotFoundException;
import co.edu.escuelaing.techcup.communications.exception.UserNotFoundException;
import co.edu.escuelaing.techcup.communications.repository.ChatRepository;
import co.edu.escuelaing.techcup.communications.service.CreateChatUseCase;
import co.edu.escuelaing.techcup.communications.service.client.TeamServiceClient;
import co.edu.escuelaing.techcup.communications.service.client.UserServiceClient;
import co.edu.escuelaing.techcup.communications.service.command.CreateChatCommand;
import co.edu.escuelaing.techcup.communications.service.command.ParticipantCommand;
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
        // Users and teams are owned by other microservices, so they are checked through their ports.
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
