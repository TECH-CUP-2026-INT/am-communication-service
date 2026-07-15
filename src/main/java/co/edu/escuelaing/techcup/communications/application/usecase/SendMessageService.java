package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.Message;
import co.edu.escuelaing.techcup.communications.domain.exception.ChatNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ChatRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.MessageRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.SendMessageUseCase;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.MessagePublisher;
import co.edu.escuelaing.techcup.communications.application.usecase.command.SendMessageCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SendMessageService implements SendMessageUseCase {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final MessagePublisher messagePublisher;

    @Override
    @Transactional
    public Message send(SendMessageCommand command) {
        Chat chat = chatRepository.findById(command.chatId())
                .orElseThrow(() -> new ChatNotFoundException(command.chatId()));
        // The domain enforces that the chat is open and the sender participates.
        Message message = chat.postMessage(command.senderId(), command.content());
        // Persist first, then publish over WebSocket (never publish an unpersisted message).
        Message saved = messageRepository.save(message);
        messagePublisher.publishChatMessage(saved);
        return saved;
    }
}
