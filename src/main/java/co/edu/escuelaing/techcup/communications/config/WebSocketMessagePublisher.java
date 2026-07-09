package co.edu.escuelaing.techcup.communications.config;

import co.edu.escuelaing.techcup.communications.dto.MessageResponse;
import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.mapper.MessageMapper;
import co.edu.escuelaing.techcup.communications.service.client.MessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebSocketMessagePublisher implements MessagePublisher {

    static final String CHAT_TOPIC = "/topic/chat/";
    static final String SUPPORT_TOPIC = "/topic/support/";

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageMapper messageMapper;

    @Override
    public void publishChatMessage(Message message) {
        send(CHAT_TOPIC + message.getChatId(), messageMapper.toResponse(message));
    }

    @Override
    public void publishSupportMessage(UUID ticketId, Message message) {
        send(SUPPORT_TOPIC + ticketId, messageMapper.toResponse(message));
    }

    /**
     * A message is broadcast only once its transaction commits: a subscriber must never see a
     * message that a later rollback would erase. The payload is mapped beforehand, while the
     * entity is still attached.
     */
    private void send(String destination, MessageResponse payload) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            messagingTemplate.convertAndSend(destination, payload);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                messagingTemplate.convertAndSend(destination, payload);
            }
        });
    }
}
