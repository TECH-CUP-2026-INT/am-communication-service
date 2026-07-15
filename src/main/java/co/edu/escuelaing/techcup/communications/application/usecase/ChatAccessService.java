package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ChatRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.SupportTicketRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.ChatAccessUseCase;
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
