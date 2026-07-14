package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.dto.ChatResponse;
import co.edu.escuelaing.techcup.communications.dto.CreateChatRequest;
import co.edu.escuelaing.techcup.communications.dto.MessageResponse;
import co.edu.escuelaing.techcup.communications.dto.PageResponse;
import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.mapper.ChatMapper;
import co.edu.escuelaing.techcup.communications.mapper.MessageMapper;
import co.edu.escuelaing.techcup.communications.service.CloseChatUseCase;
import co.edu.escuelaing.techcup.communications.service.CreateChatUseCase;
import co.edu.escuelaing.techcup.communications.service.GetChatMessagesUseCase;
import co.edu.escuelaing.techcup.communications.service.GetChatUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {

    private final CreateChatUseCase createChatUseCase;
    private final GetChatUseCase getChatUseCase;
    private final GetChatMessagesUseCase getChatMessagesUseCase;
    private final CloseChatUseCase closeChatUseCase;
    private final ChatMapper chatMapper;
    private final MessageMapper messageMapper;

    @PostMapping
    public ResponseEntity<ChatResponse> create(@Valid @RequestBody CreateChatRequest request,
                                               UriComponentsBuilder uriBuilder) {
        Chat chat = createChatUseCase.create(chatMapper.toCommand(request));
        URI location = uriBuilder.path("/chats/{id}").buildAndExpand(chat.getId()).toUri();
        return ResponseEntity.created(location).body(chatMapper.toResponse(chat));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatResponse> getById(@PathVariable UUID id,
                                                @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(chatMapper.toResponse(getChatUseCase.getById(id, caller.userId())));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<PageResponse<MessageResponse>> getMessages(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUser caller) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("sentAt").ascending());
        PageResponse<MessageResponse> body = PageResponse.of(
                getChatMessagesUseCase.getByChat(id, pageable, caller.userId()).map(messageMapper::toResponse));
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ChatResponse> close(@PathVariable UUID id,
                                              @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(chatMapper.toResponse(closeChatUseCase.close(id, caller.userId())));
    }
}
