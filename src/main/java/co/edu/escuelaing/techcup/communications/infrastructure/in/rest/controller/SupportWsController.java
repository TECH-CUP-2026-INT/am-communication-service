package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller;

import co.edu.escuelaing.techcup.communications.infrastructure.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.infrastructure.config.WebSocketMetrics;
import co.edu.escuelaing.techcup.communications.infrastructure.config.WsMessageTracing;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.SupportSendRequest;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.ReplySupportTicketUseCase;
import co.edu.escuelaing.techcup.communications.application.usecase.command.ReplySupportTicketCommand;
import io.micrometer.tracing.Tracer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.Map;


@Controller
@RequiredArgsConstructor
@Tag(
        name = "Support WebSocket",
        description = "STOMP endpoints for real-time support ticket communication."
)
public class SupportWsController {

    private final ReplySupportTicketUseCase replySupportTicketUseCase;
    private final WebSocketMetrics metrics;
    private final Tracer tracer;

    @Operation(
            summary = "Send a support message",
            description = "Receives a STOMP message from an authenticated user and forwards it to the corresponding support ticket."
    )
    @MessageMapping("support.send")
    public void send(
            @Valid @Payload SupportSendRequest request,
            AuthenticatedUser caller) {

        metrics.recordSupportMessageReceived();

        WsMessageTracing.traced(
                tracer,
                "ws.handle-message",
                Map.of(
                        "ticket.id", String.valueOf(request.ticketId()),
                        "content.length", String.valueOf(request.content().length())
                ),
                () -> replySupportTicketUseCase.reply(
                        new ReplySupportTicketCommand(
                                request.ticketId(),
                                caller.userId(),
                                request.content()
                        )
                )
        );
    }
}