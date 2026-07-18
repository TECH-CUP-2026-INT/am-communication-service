package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.domain.exception.TeamChatNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.exception.UserNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ChatRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.UserServiceClient;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.AddTeamChatParticipantUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddTeamChatParticipantService implements AddTeamChatParticipantUseCase {

    private final ChatRepository chatRepository;
    private final UserServiceClient userServiceClient;

    @Override
    @Transactional
    public Chat addParticipant(UUID teamId, UUID userId, ParticipantRole role) {
        if (!userServiceClient.exists(userId)) {
            throw new UserNotFoundException(userId);
        }
        Chat chat = chatRepository.findByTeamIdAndType(teamId, ChatType.GROUP)
                .orElseThrow(() -> new TeamChatNotFoundException(teamId));
        chat.addParticipant(userId, role);
        return chatRepository.save(chat);
    }
}
