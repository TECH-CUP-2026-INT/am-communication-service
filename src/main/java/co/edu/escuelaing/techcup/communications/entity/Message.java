package co.edu.escuelaing.techcup.communications.entity;

import co.edu.escuelaing.techcup.communications.entity.enums.MessageStatus;
import co.edu.escuelaing.techcup.communications.exception.InvalidChatOperationException;
import co.edu.escuelaing.techcup.communications.exception.MessageAlreadyReportedException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "messages",
        indexes = {
                @Index(name = "idx_message_chat", columnList = "chat_id"),
                @Index(name = "idx_message_sender", columnList = "sender_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Message {

    public static final int MAX_CONTENT_LENGTH = 2000;

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = false, updatable = false)
    private Chat chat;

    @Column(name = "sender_id", nullable = false, updatable = false)
    private UUID senderId;

    @Column(nullable = false, length = MAX_CONTENT_LENGTH)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageStatus status;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private Instant sentAt;

    Message(Chat chat, UUID senderId, String content) {
        this.id = UUID.randomUUID();
        this.chat = Objects.requireNonNull(chat, "chat is required");
        this.senderId = Objects.requireNonNull(senderId, "senderId is required");
        this.content = validateContent(content);
        this.status = MessageStatus.SENT;
        this.sentAt = Instant.now();
    }

    public void markReported() {
        if (status == MessageStatus.DELETED) {
            throw new InvalidChatOperationException("A deleted message cannot be reported: " + id);
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
            throw new InvalidChatOperationException("Message content must not be blank");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new InvalidChatOperationException("Message content exceeds " + MAX_CONTENT_LENGTH + " characters");
        }
        TextValidation.rejectControlCharacters(content, "Message content");
        return content;
    }
}
