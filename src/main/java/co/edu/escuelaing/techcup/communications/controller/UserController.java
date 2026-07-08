package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.dto.ChatResponse;
import co.edu.escuelaing.techcup.communications.mapper.ChatMapper;
import co.edu.escuelaing.techcup.communications.service.GetUserChatsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{id}/chats")
    public ResponseEntity<List<ChatResponse>> getUserChats(@PathVariable UUID id) {
        List<ChatResponse> chats = getUserChatsUseCase.getByUser(id).stream()
                .map(chatMapper::toResponse)
                .toList();
        return ResponseEntity.ok(chats);
    }
}
