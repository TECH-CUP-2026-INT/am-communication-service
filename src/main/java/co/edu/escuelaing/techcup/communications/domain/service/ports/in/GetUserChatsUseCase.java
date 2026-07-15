package co.edu.escuelaing.techcup.communications.domain.service.ports.in;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;

import java.util.List;
import java.util.UUID;

public interface GetUserChatsUseCase {

    List<Chat> getByUser(UUID userId);
}
