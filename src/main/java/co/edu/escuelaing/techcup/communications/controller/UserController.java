package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.dto.ChatResponse;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.exception.UserAccessNotAllowedException;
import co.edu.escuelaing.techcup.communications.mapper.ChatMapper;
import co.edu.escuelaing.techcup.communications.service.GetUserChatsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final GetUserChatsUseCase getUserChatsUseCase;
    private final ChatMapper chatMapper;

    /**
     * A caller may list their own chats; moderators and organizers may look up anyone's, since
     * they need it to triage reports and support tickets across the platform. {@code ADMIN} is
     * accepted too: it is cc-identity-service's administrative role, which stands in for
     * {@code MODERATOR}/{@code ORGANIZER} on tokens that service issues.
     */
    @GetMapping("/{id}/chats")
    public ResponseEntity<List<ChatResponse>> getUserChats(@PathVariable UUID id,
                                                            @AuthenticationPrincipal AuthenticatedUser caller) {
        if (!id.equals(caller.userId()) && !caller.hasAnyRole(ParticipantRole.MODERATOR.name(),
                ParticipantRole.ORGANIZER.name(), "ADMIN")) {
            throw new UserAccessNotAllowedException(caller.userId(), id);
        }
        List<ChatResponse> chats = getUserChatsUseCase.getByUser(id).stream()
                .map(chatMapper::toResponse)
                .toList();
        return ResponseEntity.ok(chats);
    }
}
