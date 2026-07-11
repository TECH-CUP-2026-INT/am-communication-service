package co.edu.escuelaing.techcup.communications.service.client;

import co.edu.escuelaing.techcup.communications.entity.Message;

import java.util.UUID;

/**
 * Outbound port for real-time delivery. Implemented by the WebSocket adapter so the
 * use cases can publish a message right after it has been persisted.
 */
public interface MessagePublisher {

    void publishChatMessage(Message message);

    void publishSupportMessage(UUID ticketId, Message message);
}
