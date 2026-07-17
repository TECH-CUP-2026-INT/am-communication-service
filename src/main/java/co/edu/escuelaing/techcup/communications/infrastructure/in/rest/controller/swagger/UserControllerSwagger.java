package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller.swagger;

import co.edu.escuelaing.techcup.communications.infrastructure.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.ChatResponse;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Users",
        description = "Operations related to users and their chats."
)
@SecurityRequirement(name = "Bearer Authentication")
public interface UserControllerSwagger {

    @Operation(
            summary = "Get a user's chats",
            description = "Returns all chats associated with the specified user. Users may access their own chats, while moderators, organizers and administrators may access chats for any user."
    )
    @ApiResponse(responseCode = "200", description = "Chats recuperados exitosamente",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChatResponse.class))))
    @ApiResponse(responseCode = "401", description = "Autenticación requerida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "El usuario no está autorizado a acceder a estos chats",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<List<ChatResponse>> getUserChats(
            @Parameter(description = "Identificador del usuario", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID id,
            AuthenticatedUser caller);
}
