package co.edu.escuelaing.techcup.communications.dto;

import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ParticipantRequest(
        @NotNull UUID userId,
        @NotNull ParticipantRole role
) {
}
