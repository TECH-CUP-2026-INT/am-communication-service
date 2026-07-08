package co.edu.escuelaing.techcup.communications.dto;

import co.edu.escuelaing.techcup.communications.entity.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SendMessageRequest(
        @NotNull UUID chatId,
        @NotNull UUID senderId,
        @NotBlank @Size(max = Message.MAX_CONTENT_LENGTH) String content
) {
}
