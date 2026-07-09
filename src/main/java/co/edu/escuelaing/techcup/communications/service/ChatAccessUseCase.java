package co.edu.escuelaing.techcup.communications.service;

import java.util.UUID;

/** Answers whether a user may read the conversation behind a chat or a support ticket. */
public interface ChatAccessUseCase {

    boolean canReadChat(UUID chatId, UUID userId);

    boolean canReadSupportTicket(UUID ticketId, UUID userId);
}
