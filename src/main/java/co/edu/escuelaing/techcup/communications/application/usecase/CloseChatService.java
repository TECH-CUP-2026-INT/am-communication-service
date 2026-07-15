package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.exception.ChatNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.exception.ParticipantNotAllowedException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ChatRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.CloseChatUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloseChatService implements CloseChatUseCase {

    private final ChatRepository chatRepository;

    @Override
    @Transactional
    public Chat close(UUID chatId, UUID callerId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException(chatId));
        if (!chat.isParticipant(callerId)) {
            throw new ParticipantNotAllowedException(callerId, chatId);
        }
        chat.close();
        return chatRepository.save(chat);
    }
}
