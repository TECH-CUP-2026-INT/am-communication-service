package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller.swagger;

import co.edu.escuelaing.techcup.communications.infrastructure.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.ResolveReportRequest;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.ErrorResponse;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.ReportedMessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(
        name = "Reports",
        description = "Operations for managing reported chat messages."
)
@SecurityRequirement(name = "Bearer Authentication")
public interface ReportControllerSwagger {

    @Operation(
            summary = "Resolve a reported message",
            description = "Resolves a reported message by assigning a resolution status, moderator note and action."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    schema = @Schema(implementation = ResolveReportRequest.class),
                    examples = @ExampleObject(value = """
                            {"resolutionStatus": "RESOLVED", "note": "Se eliminó el mensaje", "actionType": "DELETE_MESSAGE"}""")))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reporte resuelto exitosamente",
                    content = @Content(schema = @Schema(implementation = ReportedMessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Autenticación requerida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario no está autorizado a resolver reportes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Reporte no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ReportedMessageResponse> resolve(
            @Parameter(description = "Identificador del reporte", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID id,
            ResolveReportRequest request,
            AuthenticatedUser caller);
}
