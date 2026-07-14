package co.edu.escuelaing.techcup.communications.config;

import co.edu.escuelaing.techcup.communications.dto.MessageResponse;
import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.mapper.MessageMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketMessagePublisherTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private WebSocketMetrics metrics;

    @InjectMocks
    private WebSocketMessagePublisher publisher;

    private final UUID sender = UUID.randomUUID();

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private Message aMessage() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(sender, ParticipantRole.MEMBER);
        return chat.postMessage(sender, "hello");
    }

    private MessageResponse anyResponse(Message m) {
        return new MessageResponse(UUID.randomUUID(), m.getChatId(), sender, "hello", m.getStatus(), Instant.now());
    }

    private void commit() {
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
    }

    @Test
    void publishesChatMessageToChatTopic() {
        Message message = aMessage();
        when(messageMapper.toResponse(message)).thenReturn(anyResponse(message));

        publisher.publishChatMessage(message);

        verify(messagingTemplate).convertAndSend(
                eq(WebSocketMessagePublisher.CHAT_TOPIC + message.getChatId()), any(Object.class));
        verify(metrics).recordBroadcast();
    }

    @Test
    void publishesSupportMessageToSupportTopic() {
        Message message = aMessage();
        UUID ticketId = UUID.randomUUID();
        when(messageMapper.toResponse(message)).thenReturn(anyResponse(message));

        publisher.publishSupportMessage(ticketId, message);

        verify(messagingTemplate).convertAndSend(
                eq(WebSocketMessagePublisher.SUPPORT_TOPIC + ticketId), any(Object.class));
    }

    @Test
    void holdsTheBroadcastUntilTheTransactionCommits() {
        Message message = aMessage();
        when(messageMapper.toResponse(message)).thenReturn(anyResponse(message));
        TransactionSynchronizationManager.initSynchronization();

        publisher.publishChatMessage(message);
        verifyNoInteractions(messagingTemplate, metrics);

        commit();
        verify(messagingTemplate).convertAndSend(
                eq(WebSocketMessagePublisher.CHAT_TOPIC + message.getChatId()), any(Object.class));
        verify(metrics).recordBroadcast();
    }

    @Test
    void neverBroadcastsAMessageWhoseTransactionRollsBack() {
        Message message = aMessage();
        when(messageMapper.toResponse(message)).thenReturn(anyResponse(message));
        TransactionSynchronizationManager.initSynchronization();

        publisher.publishSupportMessage(UUID.randomUUID(), message);

        // The transaction rolls back: afterCommit never runs.
        verifyNoInteractions(messagingTemplate, metrics);
    }
}
