package co.edu.escuelaing.techcup.communications.domain.service.ports.in;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;

import java.util.UUID;

public interface AddTeamChatParticipantUseCase {

    Chat addParticipant(UUID teamId, UUID userId, ParticipantRole role);
}
