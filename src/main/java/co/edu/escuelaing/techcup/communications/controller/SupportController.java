package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.dto.CreateSupportTicketRequest;
import co.edu.escuelaing.techcup.communications.dto.MessageResponse;
import co.edu.escuelaing.techcup.communications.dto.ReplySupportTicketRequest;
import co.edu.escuelaing.techcup.communications.dto.SupportTicketResponse;
import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import co.edu.escuelaing.techcup.communications.mapper.MessageMapper;
import co.edu.escuelaing.techcup.communications.mapper.SupportMapper;
import co.edu.escuelaing.techcup.communications.service.CreateSupportTicketUseCase;
import co.edu.escuelaing.techcup.communications.service.EscalateConversationUseCase;
import co.edu.escuelaing.techcup.communications.service.ReplySupportTicketUseCase;
import co.edu.escuelaing.techcup.communications.service.command.CreateSupportTicketCommand;
import co.edu.escuelaing.techcup.communications.service.command.ReplySupportTicketCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/support/tickets")
@RequiredArgsConstructor
public class SupportController {

    private final CreateSupportTicketUseCase createSupportTicketUseCase;
    private final ReplySupportTicketUseCase replySupportTicketUseCase;
    private final EscalateConversationUseCase escalateConversationUseCase;
    private final SupportMapper supportMapper;
    private final MessageMapper messageMapper;

    @PostMapping
    public ResponseEntity<SupportTicketResponse> create(@Valid @RequestBody CreateSupportTicketRequest request,
                                                        @AuthenticationPrincipal AuthenticatedUser caller,
                                                        UriComponentsBuilder uriBuilder) {
        SupportTicket ticket = createSupportTicketUseCase.create(
                new CreateSupportTicketCommand(caller.userId(), request.subject()));
        URI location = uriBuilder.path("/support/tickets/{id}").buildAndExpand(ticket.getId()).toUri();
        return ResponseEntity.created(location).body(supportMapper.toResponse(ticket));
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<MessageResponse> reply(@PathVariable UUID id,
                                                 @Valid @RequestBody ReplySupportTicketRequest request,
                                                 @AuthenticationPrincipal AuthenticatedUser caller,
                                                 UriComponentsBuilder uriBuilder) {
        Message message = replySupportTicketUseCase.reply(
                new ReplySupportTicketCommand(id, caller.userId(), request.content()));
        URI location = uriBuilder.path("/messages/{id}").buildAndExpand(message.getId()).toUri();
        return ResponseEntity.created(location).body(messageMapper.toResponse(message));
    }

    @PostMapping("/{id}/escalate")
    public ResponseEntity<SupportTicketResponse> escalate(@PathVariable UUID id,
                                                           @AuthenticationPrincipal AuthenticatedUser caller) {
        SupportTicket ticket = escalateConversationUseCase.escalate(id, caller.userId());
        return ResponseEntity.ok(supportMapper.toResponse(ticket));
    }
}
