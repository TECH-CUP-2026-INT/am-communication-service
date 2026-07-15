package co.edu.escuelaing.techcup.communications.domain.service.ports.in;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;

import java.util.UUID;

public interface GetChatUseCase {

    Chat getById(UUID chatId, UUID callerId);
}
