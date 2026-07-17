package co.edu.escuelaing.techcup.communications.domain.service.ports.out;

import co.edu.escuelaing.techcup.communications.domain.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Outbound port for Message persistence. */
public interface MessageRepository {

    Optional<Message> findById(UUID id);

    Message save(Message message);

    Page<Message> findByChatId(UUID chatId, Pageable pageable);

    List<Message> findByChatIdOrderBySentAtAsc(UUID chatId);
}
