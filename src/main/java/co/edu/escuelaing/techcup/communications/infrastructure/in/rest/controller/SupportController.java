package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller;

import co.edu.escuelaing.techcup.communications.infrastructure.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.domain.model.Message;
import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.CreateSupportTicketRequest;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.MessageResponse;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.ReplySupportTicketRequest;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.SupportTicketResponse;
import co.edu.escuelaing.techcup.communications.application.mapper.MessageMapper;
import co.edu.escuelaing.techcup.communications.application.mapper.SupportMapper;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.CreateSupportTicketUseCase;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.EscalateConversationUseCase;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.ReplySupportTicketUseCase;
import co.edu.escuelaing.techcup.communications.application.usecase.command.CreateSupportTicketCommand;
import co.edu.escuelaing.techcup.communications.application.usecase.command.ReplySupportTicketCommand;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller.swagger.SupportControllerSwagger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/support/tickets")
@RequiredArgsConstructor
public class SupportController implements SupportControllerSwagger {

    private final CreateSupportTicketUseCase createSupportTicketUseCase;
    private final ReplySupportTicketUseCase replySupportTicketUseCase;
    private final EscalateConversationUseCase escalateConversationUseCase;
    private final SupportMapper supportMapper;
    private final MessageMapper messageMapper;

    @Override
    @PostMapping
    public ResponseEntity<SupportTicketResponse> create(
            @Valid @RequestBody CreateSupportTicketRequest request,
            @AuthenticationPrincipal AuthenticatedUser caller,
            UriComponentsBuilder uriBuilder) {

        SupportTicket ticket = createSupportTicketUseCase.create(
                new CreateSupportTicketCommand(
                        caller.userId(),
                        request.subject()
                ));

        URI location = uriBuilder.path("/support/tickets/{id}")
                .buildAndExpand(ticket.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(supportMapper.toResponse(ticket));
    }

    @Override
    @PostMapping("/{id}/reply")
    public ResponseEntity<MessageResponse> reply(
            @PathVariable UUID id,
            @Valid @RequestBody ReplySupportTicketRequest request,
            @AuthenticationPrincipal AuthenticatedUser caller,
            UriComponentsBuilder uriBuilder) {

        Message message = replySupportTicketUseCase.reply(
                new ReplySupportTicketCommand(
                        id,
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
    @PostMapping("/{id}/escalate")
    public ResponseEntity<SupportTicketResponse> escalate(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser caller) {

        SupportTicket ticket = escalateConversationUseCase.escalate(
                id,
                caller.userId()
        );

        return ResponseEntity.ok(
                supportMapper.toResponse(ticket)
        );
    }
}
