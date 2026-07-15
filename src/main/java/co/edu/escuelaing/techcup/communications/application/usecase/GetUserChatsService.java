package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ChatRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.GetUserChatsUseCase;
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
        return chatRepository.findAllByParticipantUserId(userId);
    }
}
