package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.repository.ChatRepository;
import co.edu.escuelaing.techcup.communications.service.CreateChatUseCase;
import co.edu.escuelaing.techcup.communications.service.command.CreateChatCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateChatService implements CreateChatUseCase {

    private final ChatRepository chatRepository;

    @Override
    @Transactional
    public Chat create(CreateChatCommand command) {
        Chat chat = Chat.create(command.type(), command.teamId());
        command.participants().forEach(p -> chat.addParticipant(p.userId(), p.role()));
        return chatRepository.save(chat);
    }
}
