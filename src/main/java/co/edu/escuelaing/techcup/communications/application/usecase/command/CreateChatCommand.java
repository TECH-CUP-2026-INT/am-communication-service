package co.edu.escuelaing.techcup.communications.application.usecase.command;

import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;

import java.util.List;
import java.util.UUID;

public record CreateChatCommand(ChatType type, UUID teamId, List<ParticipantCommand> participants) {
}
