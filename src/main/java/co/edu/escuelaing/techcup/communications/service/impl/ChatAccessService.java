package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.repository.ChatRepository;
import co.edu.escuelaing.techcup.communications.repository.SupportTicketRepository;
import co.edu.escuelaing.techcup.communications.service.ChatAccessUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatAccessService implements ChatAccessUseCase {

    private final ChatRepository chatRepository;
    private final SupportTicketRepository supportTicketRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean canReadChat(UUID chatId, UUID userId) {
        return chatRepository.isParticipant(chatId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canReadSupportTicket(UUID ticketId, UUID userId) {
        return supportTicketRepository.isParticipantOfTicketChat(ticketId, userId);
    }
}
