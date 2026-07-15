package co.edu.escuelaing.techcup.communications.infrastructure.config;

import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.MessageResponse;
import co.edu.escuelaing.techcup.communications.domain.model.Message;
import co.edu.escuelaing.techcup.communications.application.mapper.MessageMapper;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.MessagePublisher;
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
    private final WebSocketMetrics metrics;

    @Override
    public void publishChatMessage(Message message) {
        send(CHAT_TOPIC + message.getChatId(), messageMapper.toResponse(message));
    }

    @Override
    public void publishSupportMessage(UUID ticketId, Message message) {
        send(SUPPORT_TOPIC + ticketId, messageMapper.toResponse(message));
    }

    private void send(String destination, MessageResponse payload) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            broadcast(destination, payload);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                broadcast(destination, payload);
            }
        });
    }

    private void broadcast(String destination, MessageResponse payload) {
        messagingTemplate.convertAndSend(destination, payload);
        metrics.recordBroadcast();
    }
}
