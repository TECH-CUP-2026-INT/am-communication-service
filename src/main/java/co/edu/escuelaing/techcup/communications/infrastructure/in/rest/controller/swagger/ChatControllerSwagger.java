package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller.swagger;

import co.edu.escuelaing.techcup.communications.infrastructure.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.CreateChatRequest;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.ParticipantRequest;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.ChatResponse;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.ErrorResponse;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.MessageResponse;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Tag(
        name = "Chats",
        description = "Endpoints for creating, retrieving and managing chats."
)
@SecurityRequirement(name = "Bearer Authentication")
public interface ChatControllerSwagger {

    @Operation(
            summary = "Create a new chat",
            description = "Creates a chat between the specified participants."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    schema = @Schema(implementation = CreateChatRequest.class),
                    examples = @ExampleObject(name = "crear-chat-directo", value = """
                            {
                              "type": "DIRECT",
                              "teamId": null,
                              "participants": [
                                {"userId": "550e8400-e29b-41d4-a716-446655440000", "role": "MEMBER"},
                                {"userId": "660e8400-e29b-41d4-a716-446655440001", "role": "MEMBER"}
                              ]
                            }""")))
    @ApiResponse(responseCode = "201", description = "Chat creado exitosamente",
            content = @Content(schema = @Schema(implementation = ChatResponse.class),
            examples = @ExampleObject(value = """
            {
            "id": "f18c1f66-87b4-4eaf-bfdb-c6fd0fb89d7c",
            "type": "DIRECT",
            "teamId": null,
            "status": "OPEN",
            "participants": [],
            "createdAt": "2026-07-15T10:15:30Z"
            }""")))
    @ApiResponse(responseCode = "400", description = "Solicitud inválida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Autenticación requerida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<ChatResponse> create(CreateChatRequest request, UriComponentsBuilder uriBuilder);

    @Operation(
            summary = "Get chat by ID",
            description = "Returns the chat information if the authenticated user is a participant."
    )
    @ApiResponse(responseCode = "200", description = "Chat encontrado",
            content = @Content(schema = @Schema(implementation = ChatResponse.class)))
    @ApiResponse(responseCode = "401", description = "Autenticación requerida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "El usuario no es participante del chat",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Chat no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<ChatResponse> getById(
            @Parameter(description = "Identificador único del chat", example = "f18c1f66-87b4-4eaf-bfdb-c6fd0fb89d7c")
            UUID id,
            AuthenticatedUser caller);

    @Operation(
            summary = "Get chat messages",
            description = "Returns a paginated list of messages belonging to the chat."
    )
    @ApiResponse(responseCode = "200", description = "Mensajes recuperados exitosamente",
            content = @Content(schema = @Schema(implementation = PageResponse.class)))
    @ApiResponse(responseCode = "401", description = "Autenticación requerida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "El usuario no es participante del chat",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Chat no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<PageResponse<MessageResponse>> getMessages(
            @Parameter(description = "Identificador del chat", example = "f18c1f66-87b4-4eaf-bfdb-c6fd0fb89d7c")
            UUID id,
            @Parameter(description = "Número de página", example = "0")
            int page,
            @Parameter(description = "Cantidad de mensajes por página", example = "20")
            int size,
            AuthenticatedUser caller);

    @Operation(
            summary = "Close a chat",
            description = "Marks the chat as closed. After closing, no more messages can be sent."
    )
    @ApiResponse(responseCode = "200", description = "Chat cerrado exitosamente",
            content = @Content(schema = @Schema(implementation = ChatResponse.class)))
    @ApiResponse(responseCode = "401", description = "Autenticación requerida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "El usuario no puede cerrar este chat",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Chat no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<ChatResponse> close(
            @Parameter(description = "Identificador del chat", example = "f18c1f66-87b4-4eaf-bfdb-c6fd0fb89d7c")
            UUID id,
            AuthenticatedUser caller);

    @Operation(
            summary = "Join a team's group chat",
            description = """
                    Adds the caller as a participant of the GROUP chat belonging to the given team.
                    The caller must be adding themself — this endpoint exists specifically for a
                    user who just joined a team (e.g. accepted an invitation) and does not yet know
                    the chat's id, since it can only be discovered by an existing participant.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    schema = @Schema(implementation = ParticipantRequest.class),
                    examples = @ExampleObject(value = """
                            {"userId": "550e8400-e29b-41d4-a716-446655440000", "role": "MEMBER"}""")))
    @ApiResponse(responseCode = "200", description = "Participante agregado exitosamente",
            content = @Content(schema = @Schema(implementation = ChatResponse.class)))
    @ApiResponse(responseCode = "400", description = "Solicitud inválida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Autenticación requerida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Solo el propio usuario puede agregarse a sí mismo",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "No existe un chat de tipo GROUP para ese equipo, o el usuario no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "El usuario ya es participante del chat, o el chat está cerrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<ChatResponse> addTeamParticipant(
            @Parameter(description = "Identificador del equipo", example = "660e8400-e29b-41d4-a716-446655440001")
            UUID teamId,
            ParticipantRequest request,
            AuthenticatedUser caller);
}
