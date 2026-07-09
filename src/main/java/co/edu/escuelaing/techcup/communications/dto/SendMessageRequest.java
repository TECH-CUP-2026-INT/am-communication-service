package co.edu.escuelaing.techcup.communications.dto;

import co.edu.escuelaing.techcup.communications.entity.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** The sender is never supplied by the client: it is taken from the JWT. */
public record SendMessageRequest(
        @NotNull UUID chatId,
        @NotBlank @Size(max = Message.MAX_CONTENT_LENGTH) String content
) {
}
