package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request;

import co.edu.escuelaing.techcup.communications.domain.model.Faq;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record FaqRequest(
        @Schema(description = "Palabras clave asociadas a la pregunta frecuente", example = "[\"horario\", \"torneo\"]")
        @NotEmpty(message = "Debe indicar al menos una palabra clave")
        @Size(max = Faq.MAX_KEYWORDS, message = "No puede indicar más de {max} palabras clave")
        Set<@NotBlank(message = "La palabra clave no puede estar vacía")
            @Size(max = Faq.MAX_KEYWORD_LENGTH, message = "La palabra clave no puede superar {max} caracteres") String> keywords,

        @Schema(description = "Respuesta a la pregunta frecuente", example = "El torneo inicia a las 8:00 a. m.")
        @NotBlank(message = "La respuesta es obligatoria")
        @Size(max = Faq.MAX_ANSWER_LENGTH, message = "La respuesta no puede superar {max} caracteres")
        String answer
) {
}
