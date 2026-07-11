package co.edu.escuelaing.techcup.communications.service.command;

import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;

import java.util.UUID;

public record ParticipantCommand(UUID userId, ParticipantRole role) {
}
