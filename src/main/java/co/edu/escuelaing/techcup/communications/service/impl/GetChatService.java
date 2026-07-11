package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.exception.ChatNotFoundException;
import co.edu.escuelaing.techcup.communications.repository.ChatRepository;
import co.edu.escuelaing.techcup.communications.service.GetChatUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetChatService implements GetChatUseCase {

    private final ChatRepository chatRepository;

    @Override
    @Transactional(readOnly = true)
    public Chat getById(UUID chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException(chatId));
    }
}
