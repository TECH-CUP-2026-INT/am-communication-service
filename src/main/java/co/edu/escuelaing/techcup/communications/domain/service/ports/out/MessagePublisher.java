package co.edu.escuelaing.techcup.communications.domain.service.ports.out;

import co.edu.escuelaing.techcup.communications.domain.model.Message;

import java.util.UUID;

/**
 * Outbound port for real-time delivery. Implemented by the WebSocket adapter so the
 * use cases can publish a message right after it has been persisted.
 */
public interface MessagePublisher {

    void publishChatMessage(Message message);

    void publishSupportMessage(UUID ticketId, Message message);
}
