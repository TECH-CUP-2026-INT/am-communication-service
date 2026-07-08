package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.repository.ChatRepository;
import co.edu.escuelaing.techcup.communications.service.GetUserChatsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetUserChatsService implements GetUserChatsUseCase {

    private final ChatRepository chatRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Chat> getByUser(UUID userId) {
        return chatRepository.findDistinctByParticipants_UserId(userId);
    }
}
