package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller.swagger;

import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.FaqRequest;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.ErrorResponse;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.FaqResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "FAQs",
        description = "Operations for managing frequently asked questions."
)
public interface FaqControllerSwagger {

    @Operation(
            summary = "Create a new FAQ",
            description = "Creates a new frequently asked question with its associated keywords and answer."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    schema = @Schema(implementation = FaqRequest.class),
                    examples = @ExampleObject(value = """
                            {"keywords": ["horario", "torneo"], "answer": "El torneo inicia a las 8:00 a. m."}""")))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "FAQ creada exitosamente",
                    content = @Content(schema = @Schema(implementation = FaqResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<FaqResponse> create(FaqRequest request, UriComponentsBuilder uriBuilder);

    @Operation(
            summary = "List all FAQs",
            description = "Returns all frequently asked questions available in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "FAQs recuperadas exitosamente",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = FaqResponse.class))))
    })
    ResponseEntity<List<FaqResponse>> list();

    @Operation(
            summary = "Get a FAQ by ID",
            description = "Returns the FAQ identified by the specified ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "FAQ encontrada",
                    content = @Content(schema = @Schema(implementation = FaqResponse.class))),
            @ApiResponse(responseCode = "404", description = "FAQ no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<FaqResponse> getById(
            @Parameter(description = "Identificador de la FAQ", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID id);

    @Operation(
            summary = "Update a FAQ",
            description = "Updates the keywords and answer of an existing FAQ."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "FAQ actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = FaqResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "FAQ no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<FaqResponse> update(
            @Parameter(description = "Identificador de la FAQ", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID id,
            FaqRequest request);

    @Operation(
            summary = "Delete a FAQ",
            description = "Deletes the FAQ identified by the specified ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "FAQ eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "FAQ no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    void delete(
            @Parameter(description = "Identificador de la FAQ", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID id);
}
