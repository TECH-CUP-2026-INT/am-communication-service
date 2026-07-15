package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller.swagger;

import co.edu.escuelaing.techcup.communications.infrastructure.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.ReportMessageRequest;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.SendMessageRequest;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.ErrorResponse;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.MessageResponse;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Tag(
        name = "Messages",
        description = "Operations for sending and reporting chat messages."
)
@SecurityRequirement(name = "Bearer Authentication")
public interface MessageControllerSwagger {

    @Operation(
            summary = "Send a message",
            description = "Sends a new message to an existing chat."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    schema = @Schema(implementation = SendMessageRequest.class),
                    examples = @ExampleObject(value = """
                            {"chatId": "f18c1f66-87b4-4eaf-bfdb-c6fd0fb89d7c", "content": "Hola, necesito ayuda con mi reserva"}""")))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mensaje enviado exitosamente",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Autenticación requerida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Chat no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<MessageResponse> send(SendMessageRequest request, AuthenticatedUser caller, UriComponentsBuilder uriBuilder);

    @Operation(
            summary = "Report a message",
            description = "Reports a message for moderation by providing a reason."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    schema = @Schema(implementation = ReportMessageRequest.class),
                    examples = @ExampleObject(value = """
                            {"reason": "Contenido ofensivo"}""")))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mensaje reportado exitosamente",
                    content = @Content(schema = @Schema(implementation = ReportedMessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Autenticación requerida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Mensaje no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ReportedMessageResponse report(
            @Parameter(description = "Identificador del mensaje", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID id,
            ReportMessageRequest request,
            AuthenticatedUser caller);
}
