package co.edu.escuelaing.techcup.communications.service;

import co.edu.escuelaing.techcup.communications.entity.Chat;

import java.util.UUID;

public interface CloseChatUseCase {

    Chat close(UUID chatId, UUID callerId);
}
