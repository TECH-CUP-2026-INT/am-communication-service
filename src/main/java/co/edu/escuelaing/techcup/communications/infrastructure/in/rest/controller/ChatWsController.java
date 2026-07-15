package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller;

import co.edu.escuelaing.techcup.communications.infrastructure.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.infrastructure.config.WebSocketMetrics;
import co.edu.escuelaing.techcup.communications.infrastructure.config.WsMessageTracing;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.SendMessageRequest;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.SendMessageUseCase;
import co.edu.escuelaing.techcup.communications.application.usecase.command.SendMessageCommand;
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
        name = "WebSocket Chat",
        description = "STOMP endpoints for real-time chat communication."
)
public class ChatWsController {

    private final SendMessageUseCase sendMessageUseCase;
    private final WebSocketMetrics metrics;
    private final Tracer tracer;

    @Operation(
            summary = "Send a chat message",
            description = """
                Receives a STOMP message sent to '/app/chat.send'.
                The message is validated, traced and forwarded to the chat service.
                """
    )


    @MessageMapping("chat.send")
    public void send(@Valid @Payload SendMessageRequest request, AuthenticatedUser caller) {
        metrics.recordChatMessageReceived();
        WsMessageTracing.traced(tracer, "ws.handle-message", Map.of(
                        "chat.id", String.valueOf(request.chatId()),
                        "content.length", String.valueOf(request.content().length())),
                () -> sendMessageUseCase.send(
                        new SendMessageCommand(request.chatId(), caller.userId(), request.content())));
    }
}
