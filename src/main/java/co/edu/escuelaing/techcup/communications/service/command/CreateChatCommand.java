package co.edu.escuelaing.techcup.communications.service.command;

import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;

import java.util.List;
import java.util.UUID;

public record CreateChatCommand(ChatType type, UUID teamId, List<ParticipantCommand> participants) {
}
