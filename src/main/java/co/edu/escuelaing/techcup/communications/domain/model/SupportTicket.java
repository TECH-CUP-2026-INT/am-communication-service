package co.edu.escuelaing.techcup.communications.domain.model;

import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportTicketStatus;
import co.edu.escuelaing.techcup.communications.domain.exception.InvalidChatOperationException;
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
public class SupportTicket {

    public static final int MAX_SUBJECT_LENGTH = 200;

    private UUID id;
    private Chat chat;
    private UUID requesterId;
    private String subject;
    private SupportTicketStatus status;
    private SupportLevel currentLevel;
    private UUID assignedTo;
    private Instant createdAt;
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

    // Reconstruction from a persisted row: every column maps to one argument, so the parameter
    // count mirrors the stored state and a builder would only add indirection here.
    @SuppressWarnings("java:S107")
    public static SupportTicket fromPersistence(UUID id, Chat chat, UUID requesterId, String subject,
                                                 SupportTicketStatus status, SupportLevel currentLevel,
                                                 UUID assignedTo, Instant createdAt, Instant updatedAt) {
        SupportTicket ticket = new SupportTicket();
        ticket.id = id;
        ticket.chat = chat;
        ticket.requesterId = requesterId;
        ticket.subject = subject;
        ticket.status = status;
        ticket.currentLevel = currentLevel;
        ticket.assignedTo = assignedTo;
        ticket.createdAt = createdAt;
        ticket.updatedAt = updatedAt;
        return ticket;
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
            throw new InvalidChatOperationException("El ticket de soporte ya fue resuelto: " + id);
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static String validateSubject(String subject) {
        if (subject == null || subject.isBlank()) {
            throw new InvalidChatOperationException("El asunto del ticket de soporte no puede estar vacío");
        }
        if (subject.length() > MAX_SUBJECT_LENGTH) {
            throw new InvalidChatOperationException("El asunto supera los " + MAX_SUBJECT_LENGTH + " caracteres");
        }
        TextValidation.rejectControlCharacters(subject, "El asunto del ticket de soporte");
        return subject;
    }
}
