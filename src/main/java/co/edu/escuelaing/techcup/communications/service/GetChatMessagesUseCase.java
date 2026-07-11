package co.edu.escuelaing.techcup.communications.service;

import co.edu.escuelaing.techcup.communications.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GetChatMessagesUseCase {

    Page<Message> getByChat(UUID chatId, Pageable pageable);
}
