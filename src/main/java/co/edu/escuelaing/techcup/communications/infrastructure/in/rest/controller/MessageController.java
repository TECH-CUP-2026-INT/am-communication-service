package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller;

import co.edu.escuelaing.techcup.communications.infrastructure.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.domain.model.Message;
import co.edu.escuelaing.techcup.communications.domain.model.ReportedMessage;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.MessageResponse;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.ReportMessageRequest;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.ReportedMessageResponse;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.SendMessageRequest;
import co.edu.escuelaing.techcup.communications.application.mapper.MessageMapper;
import co.edu.escuelaing.techcup.communications.application.mapper.ReportMapper;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.ReportMessageUseCase;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.SendMessageUseCase;
import co.edu.escuelaing.techcup.communications.application.usecase.command.ReportMessageCommand;
import co.edu.escuelaing.techcup.communications.application.usecase.command.SendMessageCommand;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller.swagger.MessageControllerSwagger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController implements MessageControllerSwagger {

    private final SendMessageUseCase sendMessageUseCase;
    private final ReportMessageUseCase reportMessageUseCase;
    private final MessageMapper messageMapper;
    private final ReportMapper reportMapper;

    @Override
    @PostMapping
    public ResponseEntity<MessageResponse> send(
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal AuthenticatedUser caller,
            UriComponentsBuilder uriBuilder) {

        Message message = sendMessageUseCase.send(
                new SendMessageCommand(
                        request.chatId(),
                        caller.userId(),
                        request.content()
                ));

        URI location = uriBuilder.path("/messages/{id}")
                .buildAndExpand(message.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(messageMapper.toResponse(message));
    }

    @Override
    @PostMapping("/{id}/report")
    @ResponseStatus(HttpStatus.CREATED)
    public ReportedMessageResponse report(
            @PathVariable UUID id,
            @Valid @RequestBody ReportMessageRequest request,
            @AuthenticationPrincipal AuthenticatedUser caller) {

        ReportedMessage report = reportMessageUseCase.report(
                new ReportMessageCommand(
                        id,
                        caller.userId(),
                        request.reason()
                ));

        return reportMapper.toResponse(report);
    }
}
