package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.dto.MessageResponse;
import co.edu.escuelaing.techcup.communications.dto.SendMessageRequest;
import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.mapper.MessageMapper;
import co.edu.escuelaing.techcup.communications.service.SendMessageUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final SendMessageUseCase sendMessageUseCase;
    private final MessageMapper messageMapper;

    @PostMapping
    public ResponseEntity<MessageResponse> send(@Valid @RequestBody SendMessageRequest request,
                                                UriComponentsBuilder uriBuilder) {
        Message message = sendMessageUseCase.send(messageMapper.toCommand(request));
        URI location = uriBuilder.path("/messages/{id}").buildAndExpand(message.getId()).toUri();
        return ResponseEntity.created(location).body(messageMapper.toResponse(message));
    }
}
