package co.edu.escuelaing.techcup.communications.service;

import co.edu.escuelaing.techcup.communications.entity.Chat;

import java.util.UUID;

public interface GetChatUseCase {

    Chat getById(UUID chatId);
}
