package co.edu.escuelaing.techcup.communications.config;

import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.mapper.MessageMapper;
import co.edu.escuelaing.techcup.communications.service.client.MessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

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
        messagingTemplate.convertAndSend(CHAT_TOPIC + message.getChatId(), messageMapper.toResponse(message));
    }

    @Override
    public void publishSupportMessage(UUID ticketId, Message message) {
        messagingTemplate.convertAndSend(SUPPORT_TOPIC + ticketId, messageMapper.toResponse(message));
    }
}
