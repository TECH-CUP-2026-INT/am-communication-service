package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller.swagger;

import co.edu.escuelaing.techcup.communications.infrastructure.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.CreateSupportTicketRequest;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.ReplySupportTicketRequest;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.ErrorResponse;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.MessageResponse;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.SupportTicketResponse;
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
        name = "Support",
        description = "Operations for managing support tickets."
)
@SecurityRequirement(name = "Bearer Authentication")
public interface SupportControllerSwagger {

    @Operation(
            summary = "Create a support ticket",
            description = "Creates a new support ticket for the authenticated user."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    schema = @Schema(implementation = CreateSupportTicketRequest.class),
                    examples = @ExampleObject(value = """
                            {"subject": "No puedo ingresar al torneo"}""")))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket de soporte creado exitosamente",
                    content = @Content(schema = @Schema(implementation = SupportTicketResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Autenticación requerida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SupportTicketResponse> create(CreateSupportTicketRequest request, AuthenticatedUser caller, UriComponentsBuilder uriBuilder);

    @Operation(
            summary = "Reply to a support ticket",
            description = "Adds a new message to an existing support ticket."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    schema = @Schema(implementation = ReplySupportTicketRequest.class),
                    examples = @ExampleObject(value = """
                            {"content": "Ya revisamos tu caso, por favor intenta de nuevo"}""")))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Respuesta enviada exitosamente",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Autenticación requerida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ticket de soporte no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<MessageResponse> reply(
            @Parameter(description = "Identificador del ticket de soporte", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID id,
            ReplySupportTicketRequest request,
            AuthenticatedUser caller,
            UriComponentsBuilder uriBuilder);

    @Operation(
            summary = "Escalate a support ticket",
            description = "Escalates a support ticket for further review or higher-level assistance."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket de soporte escalado exitosamente",
                    content = @Content(schema = @Schema(implementation = SupportTicketResponse.class))),
            @ApiResponse(responseCode = "401", description = "Autenticación requerida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario no está autorizado a escalar este ticket",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ticket de soporte no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SupportTicketResponse> escalate(
            @Parameter(description = "Identificador del ticket de soporte", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID id,
            AuthenticatedUser caller);
}
