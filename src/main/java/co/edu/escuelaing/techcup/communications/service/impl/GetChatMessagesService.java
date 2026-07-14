package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.exception.ChatNotFoundException;
import co.edu.escuelaing.techcup.communications.exception.ParticipantNotAllowedException;
import co.edu.escuelaing.techcup.communications.repository.ChatRepository;
import co.edu.escuelaing.techcup.communications.repository.MessageRepository;
import co.edu.escuelaing.techcup.communications.service.GetChatMessagesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetChatMessagesService implements GetChatMessagesUseCase {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Message> getByChat(UUID chatId, Pageable pageable, UUID callerId) {
        if (!chatRepository.existsById(chatId)) {
            throw new ChatNotFoundException(chatId);
        }
        if (!chatRepository.isParticipant(chatId, callerId)) {
            throw new ParticipantNotAllowedException(callerId, chatId);
        }
        return messageRepository.findByChat_Id(chatId, pageable);
    }
}
