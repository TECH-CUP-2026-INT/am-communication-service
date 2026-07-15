package co.edu.escuelaing.techcup.communications.domain.model;

import co.edu.escuelaing.techcup.communications.domain.model.enums.MessageStatus;
import co.edu.escuelaing.techcup.communications.domain.exception.InvalidChatOperationException;
import co.edu.escuelaing.techcup.communications.domain.exception.MessageAlreadyReportedException;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Message {

    public static final int MAX_CONTENT_LENGTH = 2000;

    private UUID id;
    private Chat chat;
    private UUID senderId;
    private String content;
    private MessageStatus status;
    private Instant sentAt;

    Message(Chat chat, UUID senderId, String content) {
        this.id = UUID.randomUUID();
        this.chat = Objects.requireNonNull(chat, "chat is required");
        this.senderId = Objects.requireNonNull(senderId, "senderId is required");
        this.content = validateContent(content);
        this.status = MessageStatus.SENT;
        this.sentAt = Instant.now();
    }

    public static Message fromPersistence(UUID id, Chat chat, UUID senderId, String content,
                                          MessageStatus status, Instant sentAt) {
        Message message = new Message();
        message.id = id;
        message.chat = chat;
        message.senderId = senderId;
        message.content = content;
        message.status = status;
        message.sentAt = sentAt;
        return message;
    }

    public void markReported() {
        if (status == MessageStatus.DELETED) {
            throw new InvalidChatOperationException("No se puede reportar un mensaje eliminado: " + id);
        }
        if (status == MessageStatus.REPORTED) {
            throw new MessageAlreadyReportedException(id);
        }
        this.status = MessageStatus.REPORTED;
    }

    public void markDeleted() {
        this.status = MessageStatus.DELETED;
    }

    public UUID getChatId() {
        return chat.getId();
    }

    private static String validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new InvalidChatOperationException("El contenido del mensaje no puede estar vacío");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new InvalidChatOperationException("El contenido del mensaje supera los " + MAX_CONTENT_LENGTH + " caracteres");
        }
        TextValidation.rejectControlCharacters(content, "El contenido del mensaje");
        return content;
    }
}
