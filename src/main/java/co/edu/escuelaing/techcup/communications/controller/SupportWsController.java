package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.config.WebSocketMetrics;
import co.edu.escuelaing.techcup.communications.dto.SupportSendRequest;
import co.edu.escuelaing.techcup.communications.service.ReplySupportTicketUseCase;
import co.edu.escuelaing.techcup.communications.service.command.ReplySupportTicketCommand;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

/**
 * STOMP entry point for support messages. The sender is the session principal established on
 * CONNECT. The reply use case persists the message and then publishes it to /topic/support/{ticketId}.
 */
@Controller
@RequiredArgsConstructor
public class SupportWsController {

    private final ReplySupportTicketUseCase replySupportTicketUseCase;
    private final WebSocketMetrics metrics;
    private final Tracer tracer;

    @MessageMapping("support.send")
    public void send(@Valid @Payload SupportSendRequest request, AuthenticatedUser caller) {
        metrics.recordSupportMessageReceived();
        Span span = tracer.nextSpan().name("ws.handle-message")
                .tag("ticket.id", String.valueOf(request.ticketId()))
                .tag("content.length", String.valueOf(request.content().length()))
                .start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            replySupportTicketUseCase.reply(
                    new ReplySupportTicketCommand(request.ticketId(), caller.userId(), request.content()));
        } catch (RuntimeException ex) {
            span.error(ex);
            throw ex;
        } finally {
            span.end();
        }
    }
}
