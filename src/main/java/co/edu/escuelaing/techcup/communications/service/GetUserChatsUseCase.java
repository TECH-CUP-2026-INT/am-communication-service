package co.edu.escuelaing.techcup.communications.service;

import co.edu.escuelaing.techcup.communications.entity.Chat;

import java.util.List;
import java.util.UUID;

public interface GetUserChatsUseCase {

    List<Chat> getByUser(UUID userId);
}
