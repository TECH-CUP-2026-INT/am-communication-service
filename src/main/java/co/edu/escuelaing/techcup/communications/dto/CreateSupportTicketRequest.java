package co.edu.escuelaing.techcup.communications.dto;

import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateSupportTicketRequest(
        @NotNull UUID requesterId,
        @NotBlank @Size(max = SupportTicket.MAX_SUBJECT_LENGTH) String subject
) {
}
