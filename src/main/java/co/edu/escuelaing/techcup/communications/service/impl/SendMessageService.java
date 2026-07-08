package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.exception.ChatNotFoundException;
import co.edu.escuelaing.techcup.communications.repository.ChatRepository;
import co.edu.escuelaing.techcup.communications.repository.MessageRepository;
import co.edu.escuelaing.techcup.communications.service.SendMessageUseCase;
import co.edu.escuelaing.techcup.communications.service.command.SendMessageCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SendMessageService implements SendMessageUseCase {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    @Override
    @Transactional
    public Message send(SendMessageCommand command) {
        Chat chat = chatRepository.findById(command.chatId())
                .orElseThrow(() -> new ChatNotFoundException(command.chatId()));
        // The domain enforces that the chat is open and the sender participates.
        Message message = chat.postMessage(command.senderId(), command.content());
        // Persist first; real-time publishing and notifications are added by later adapters.
        return messageRepository.save(message);
    }
}
