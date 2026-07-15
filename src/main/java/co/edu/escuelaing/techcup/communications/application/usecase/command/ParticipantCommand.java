package co.edu.escuelaing.techcup.communications.application.usecase.command;

import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;

import java.util.UUID;

public record ParticipantCommand(UUID userId, ParticipantRole role) {
}
