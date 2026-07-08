package co.edu.escuelaing.techcup.communications.dto;

import co.edu.escuelaing.techcup.communications.entity.ReportedMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ReportMessageRequest(
        @NotNull UUID reporterId,
        @NotBlank @Size(max = ReportedMessage.MAX_REASON_LENGTH) String reason
) {
}
