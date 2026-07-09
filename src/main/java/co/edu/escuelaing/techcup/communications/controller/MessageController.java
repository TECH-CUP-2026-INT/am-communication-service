package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.dto.MessageResponse;
import co.edu.escuelaing.techcup.communications.dto.ReportMessageRequest;
import co.edu.escuelaing.techcup.communications.dto.ReportedMessageResponse;
import co.edu.escuelaing.techcup.communications.dto.SendMessageRequest;
import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.entity.ReportedMessage;
import co.edu.escuelaing.techcup.communications.mapper.MessageMapper;
import co.edu.escuelaing.techcup.communications.mapper.ReportMapper;
import co.edu.escuelaing.techcup.communications.service.ReportMessageUseCase;
import co.edu.escuelaing.techcup.communications.service.SendMessageUseCase;
import co.edu.escuelaing.techcup.communications.service.command.ReportMessageCommand;
import co.edu.escuelaing.techcup.communications.service.command.SendMessageCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final SendMessageUseCase sendMessageUseCase;
    private final ReportMessageUseCase reportMessageUseCase;
    private final MessageMapper messageMapper;
    private final ReportMapper reportMapper;

    @PostMapping
    public ResponseEntity<MessageResponse> send(@Valid @RequestBody SendMessageRequest request,
                                                @AuthenticationPrincipal AuthenticatedUser caller,
                                                UriComponentsBuilder uriBuilder) {
        Message message = sendMessageUseCase.send(
                new SendMessageCommand(request.chatId(), caller.userId(), request.content()));
        URI location = uriBuilder.path("/messages/{id}").buildAndExpand(message.getId()).toUri();
        return ResponseEntity.created(location).body(messageMapper.toResponse(message));
    }

    @PostMapping("/{id}/report")
    @ResponseStatus(HttpStatus.CREATED)
    public ReportedMessageResponse report(@PathVariable UUID id,
                                          @Valid @RequestBody ReportMessageRequest request,
                                          @AuthenticationPrincipal AuthenticatedUser caller) {
        ReportedMessage report = reportMessageUseCase.report(
                new ReportMessageCommand(id, caller.userId(), request.reason()));
        return reportMapper.toResponse(report);
    }
}
