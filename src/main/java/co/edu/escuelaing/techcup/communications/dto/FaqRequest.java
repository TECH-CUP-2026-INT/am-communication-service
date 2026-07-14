package co.edu.escuelaing.techcup.communications.dto;

import co.edu.escuelaing.techcup.communications.entity.Faq;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record FaqRequest(
        @NotEmpty @Size(max = Faq.MAX_KEYWORDS) Set<@NotBlank @Size(max = Faq.MAX_KEYWORD_LENGTH) String> keywords,
        @NotBlank @Size(max = Faq.MAX_ANSWER_LENGTH) String answer
) {
}
