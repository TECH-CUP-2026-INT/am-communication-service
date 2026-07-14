package co.edu.escuelaing.techcup.communications.dto;

import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateChatRequest(
        @NotNull ChatType type,
        UUID teamId,
        @NotEmpty @Valid List<ParticipantRequest> participants
) {
}
