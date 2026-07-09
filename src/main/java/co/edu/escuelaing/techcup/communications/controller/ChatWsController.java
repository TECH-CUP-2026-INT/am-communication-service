package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.dto.SendMessageRequest;
import co.edu.escuelaing.techcup.communications.service.SendMessageUseCase;
import co.edu.escuelaing.techcup.communications.service.command.SendMessageCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

/**
 * STOMP entry point for chat messages. The send use case persists the message and
 * then publishes it to /topic/chat/{chatId}, so this handler returns nothing.
 */
@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final SendMessageUseCase sendMessageUseCase;

    @MessageMapping("chat.send")
    public void send(@Valid @Payload SendMessageRequest request) {
        sendMessageUseCase.send(new SendMessageCommand(request.chatId(), request.senderId(), request.content()));
    }
}
