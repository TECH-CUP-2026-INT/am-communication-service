package co.edu.escuelaing.techcup.communications.domain.service.ports.out;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Outbound port for Chat persistence. */
public interface ChatRepository {

    Optional<Chat> findById(UUID id);

    boolean existsById(UUID id);

    Chat save(Chat chat);

    List<Chat> findAllByParticipantUserId(UUID userId);

    boolean isParticipant(UUID chatId, UUID userId);

    Optional<Chat> findByTeamIdAndType(UUID teamId, ChatType type);
}
