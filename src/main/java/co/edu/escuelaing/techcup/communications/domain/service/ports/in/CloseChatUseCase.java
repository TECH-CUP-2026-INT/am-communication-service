package co.edu.escuelaing.techcup.communications.domain.service.ports.in;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;

import java.util.UUID;

public interface CloseChatUseCase {

    Chat close(UUID chatId, UUID callerId);
}
