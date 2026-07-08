package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.dto.ChatResponse;
import co.edu.escuelaing.techcup.communications.dto.CreateChatRequest;
import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.mapper.ChatMapper;
import co.edu.escuelaing.techcup.communications.service.CreateChatUseCase;
import co.edu.escuelaing.techcup.communications.service.GetChatUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    private final ChatMapper chatMapper;

    @PostMapping
    public ResponseEntity<ChatResponse> create(@Valid @RequestBody CreateChatRequest request,
                                               UriComponentsBuilder uriBuilder) {
        Chat chat = createChatUseCase.create(chatMapper.toCommand(request));
        URI location = uriBuilder.path("/chats/{id}").buildAndExpand(chat.getId()).toUri();
        return ResponseEntity.created(location).body(chatMapper.toResponse(chat));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(chatMapper.toResponse(getChatUseCase.getById(id)));
    }
}
