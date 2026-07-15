package co.edu.escuelaing.techcup.communications.domain.service.ports.in;

import co.edu.escuelaing.techcup.communications.domain.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GetChatMessagesUseCase {

    Page<Message> getByChat(UUID chatId, Pageable pageable, UUID callerId);
}
