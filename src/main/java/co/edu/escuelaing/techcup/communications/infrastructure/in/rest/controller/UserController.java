package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller;

import co.edu.escuelaing.techcup.communications.infrastructure.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.ChatResponse;
import co.edu.escuelaing.techcup.communications.domain.exception.UserAccessNotAllowedException;
import co.edu.escuelaing.techcup.communications.application.mapper.ChatMapper;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.GetUserChatsUseCase;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller.swagger.UserControllerSwagger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController implements UserControllerSwagger {

    private final GetUserChatsUseCase getUserChatsUseCase;
    private final ChatMapper chatMapper;

    /**
     * A caller may list their own chats; moderators and organizers may look up anyone's,
     * since they need it to triage reports and support tickets across the platform.
     * ADMIN is also accepted because it is issued by the identity service.
     */
    @Override
    @GetMapping("/{id}/chats")
    public ResponseEntity<List<ChatResponse>> getUserChats(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser caller) {

        if (!id.equals(caller.userId()) &&
                !caller.hasAnyRole(
                        ParticipantRole.MODERATOR.name(),
                        ParticipantRole.ORGANIZER.name(),
                        "ADMIN")) {

            throw new UserAccessNotAllowedException(caller.userId(), id);
        }

        List<ChatResponse> chats = getUserChatsUseCase.getByUser(id)
                .stream()
                .map(chatMapper::toResponse)
                .toList();

        return ResponseEntity.ok(chats);
    }
}
