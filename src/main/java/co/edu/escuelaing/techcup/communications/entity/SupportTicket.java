package co.edu.escuelaing.techcup.communications.entity;

import co.edu.escuelaing.techcup.communications.entity.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.entity.enums.SupportTicketStatus;
import co.edu.escuelaing.techcup.communications.exception.InvalidChatOperationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
        name = "support_tickets",
        indexes = {
                @Index(name = "idx_ticket_requester", columnList = "requester_id"),
                @Index(name = "idx_ticket_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class SupportTicket {

    public static final int MAX_SUBJECT_LENGTH = 200;

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = false, updatable = false, unique = true)
    private Chat chat;

    @Column(name = "requester_id", nullable = false, updatable = false)
    private UUID requesterId;

    @Column(nullable = false, length = MAX_SUBJECT_LENGTH)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SupportTicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_level", nullable = false, length = 20)
    private SupportLevel currentLevel;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    private SupportTicket(Chat chat, UUID requesterId, String subject) {
        this.id = UUID.randomUUID();
        this.chat = Objects.requireNonNull(chat, "chat is required");
        this.requesterId = Objects.requireNonNull(requesterId, "requesterId is required");
        this.subject = validateSubject(subject);
        this.status = SupportTicketStatus.OPEN;
        this.currentLevel = SupportLevel.FAQ;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static SupportTicket open(Chat chat, UUID requesterId, String subject) {
        return new SupportTicket(chat, requesterId, subject);
    }

    /**
     * Advances the ticket to the given level and flags it as escalated.
     * Terminal (resolved) tickets cannot be escalated.
     */
    public void escalateTo(SupportLevel nextLevel) {
        ensureNotResolved();
        Objects.requireNonNull(nextLevel, "nextLevel is required");
        this.currentLevel = nextLevel;
        this.status = SupportTicketStatus.ESCALATED;
        touch();
    }

    public void assignTo(UUID agentId) {
        ensureNotResolved();
        this.assignedTo = Objects.requireNonNull(agentId, "agentId is required");
        this.status = SupportTicketStatus.IN_PROGRESS;
        touch();
    }

    public void resolve() {
        ensureNotResolved();
        this.status = SupportTicketStatus.RESOLVED;
        touch();
    }

    public void markPending() {
        ensureNotResolved();
        this.currentLevel = SupportLevel.PENDING;
        this.status = SupportTicketStatus.PENDING;
        touch();
    }

    public UUID getChatId() {
        return chat.getId();
    }

    public boolean isResolved() {
        return status == SupportTicketStatus.RESOLVED;
    }

    private void ensureNotResolved() {
        if (status == SupportTicketStatus.RESOLVED) {
            throw new InvalidChatOperationException("Support ticket is already resolved: " + id);
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static String validateSubject(String subject) {
        if (subject == null || subject.isBlank()) {
            throw new InvalidChatOperationException("Support ticket subject must not be blank");
        }
        if (subject.length() > MAX_SUBJECT_LENGTH) {
            throw new InvalidChatOperationException("Subject exceeds " + MAX_SUBJECT_LENGTH + " characters");
        }
        TextValidation.rejectControlCharacters(subject, "Support ticket subject");
        return subject;
    }
}
