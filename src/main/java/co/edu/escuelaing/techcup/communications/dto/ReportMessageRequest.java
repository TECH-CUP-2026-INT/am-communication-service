package co.edu.escuelaing.techcup.communications.dto;

import co.edu.escuelaing.techcup.communications.entity.ReportedMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReportMessageRequest(
        @NotBlank @Size(max = ReportedMessage.MAX_REASON_LENGTH) String reason
) {
}
