package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.config.WebSocketMetrics;
import co.edu.escuelaing.techcup.communications.dto.SendMessageRequest;
import co.edu.escuelaing.techcup.communications.service.SendMessageUseCase;
import co.edu.escuelaing.techcup.communications.service.command.SendMessageCommand;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

/**
 * STOMP entry point for chat messages. The sender is the session principal established on CONNECT.
 * The send use case persists the message and then publishes it to /topic/chat/{chatId}, so this
 * handler returns nothing.
 */
@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final SendMessageUseCase sendMessageUseCase;
    private final WebSocketMetrics metrics;
    private final Tracer tracer;

    @MessageMapping("chat.send")
    public void send(@Valid @Payload SendMessageRequest request, AuthenticatedUser caller) {
        metrics.recordChatMessageReceived();
        Span span = tracer.nextSpan().name("ws.handle-message")
                .tag("chat.id", String.valueOf(request.chatId()))
                .tag("content.length", String.valueOf(request.content().length()))
                .start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            sendMessageUseCase.send(new SendMessageCommand(request.chatId(), caller.userId(), request.content()));
        } catch (RuntimeException ex) {
            span.error(ex);
            throw ex;
        } finally {
            span.end();
        }
    }
}
