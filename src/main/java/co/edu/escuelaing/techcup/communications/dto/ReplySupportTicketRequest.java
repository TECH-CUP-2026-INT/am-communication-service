package co.edu.escuelaing.techcup.communications.dto;

import co.edu.escuelaing.techcup.communications.entity.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReplySupportTicketRequest(
        @NotBlank @Size(max = Message.MAX_CONTENT_LENGTH) String content
) {
}
