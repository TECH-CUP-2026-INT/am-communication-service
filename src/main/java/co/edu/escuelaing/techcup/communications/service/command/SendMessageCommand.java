package co.edu.escuelaing.techcup.communications.service.command;

import java.util.UUID;

public record SendMessageCommand(UUID chatId, UUID senderId, String content) {
}
