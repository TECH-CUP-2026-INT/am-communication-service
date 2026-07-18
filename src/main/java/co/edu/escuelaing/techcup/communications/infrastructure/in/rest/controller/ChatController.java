package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller;

import co.edu.escuelaing.techcup.communications.infrastructure.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.domain.exception.ParticipantNotAllowedException;
import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.ChatResponse;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.CreateChatRequest;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.ParticipantRequest;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.MessageResponse;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.PageResponse;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller.swagger.ChatControllerSwagger;
import co.edu.escuelaing.techcup.communications.application.mapper.ChatMapper;
import co.edu.escuelaing.techcup.communications.application.mapper.MessageMapper;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.AddTeamChatParticipantUseCase;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.CloseChatUseCase;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.CreateChatUseCase;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.GetChatMessagesUseCase;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.GetChatUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController implements ChatControllerSwagger {

    private final CreateChatUseCase createChatUseCase;
    private final GetChatUseCase getChatUseCase;
    private final GetChatMessagesUseCase getChatMessagesUseCase;
    private final CloseChatUseCase closeChatUseCase;
    private final AddTeamChatParticipantUseCase addTeamChatParticipantUseCase;
    private final ChatMapper chatMapper;
    private final MessageMapper messageMapper;

    @Override
    @PostMapping
    public ResponseEntity<ChatResponse> create(
            @Valid @RequestBody CreateChatRequest request,
            UriComponentsBuilder uriBuilder) {

        Chat chat = createChatUseCase.create(chatMapper.toCommand(request));
        URI location = uriBuilder.path("/chats/{id}")
                .buildAndExpand(chat.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(chatMapper.toResponse(chat));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ChatResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser caller) {

        return ResponseEntity.ok(
                chatMapper.toResponse(
                        getChatUseCase.getById(id, caller.userId())
                )
        );
    }

    @Override
    @GetMapping("/{id}/messages")
    public ResponseEntity<PageResponse<MessageResponse>> getMessages(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUser caller) {

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by("sentAt").ascending());

        PageResponse<MessageResponse> body = PageResponse.of(
                getChatMessagesUseCase
                        .getByChat(id, pageable, caller.userId())
                        .map(messageMapper::toResponse));

        return ResponseEntity.ok(body);
    }

    @Override
    @PostMapping("/{id}/close")
    public ResponseEntity<ChatResponse> close(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser caller) {

        return ResponseEntity.ok(
                chatMapper.toResponse(
                        closeChatUseCase.close(id, caller.userId())
                )
        );
    }

    @Override
    @PostMapping("/team/{teamId}/participants")
    public ResponseEntity<ChatResponse> addTeamParticipant(
            @PathVariable UUID teamId,
            @Valid @RequestBody ParticipantRequest request,
            @AuthenticationPrincipal AuthenticatedUser caller) {

        if (!caller.userId().equals(request.userId())) {
            throw new ParticipantNotAllowedException(caller.userId(), teamId);
        }

        Chat chat = addTeamChatParticipantUseCase.addParticipant(teamId, request.userId(), request.role());
        return ResponseEntity.ok(chatMapper.toResponse(chat));
    }
}
