package co.edu.escuelaing.techcup.communications.entity;

import co.edu.escuelaing.techcup.communications.entity.enums.ReportStatus;
import co.edu.escuelaing.techcup.communications.exception.InvalidChatOperationException;
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
import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "reported_messages",
        indexes = {
                @Index(name = "idx_report_message", columnList = "message_id"),
                @Index(name = "idx_report_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class ReportedMessage {

    public static final int MAX_REASON_LENGTH = 500;

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false, updatable = false)
    private Message message;

    @Column(name = "reporter_id", nullable = false, updatable = false)
    private UUID reporterId;

    @Column(nullable = false, length = MAX_REASON_LENGTH)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @Column(length = MAX_REASON_LENGTH)
    private String resolution;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    private ReportedMessage(Message message, UUID reporterId, String reason) {
        this.id = UUID.randomUUID();
        this.message = Objects.requireNonNull(message, "message is required");
        this.reporterId = Objects.requireNonNull(reporterId, "reporterId is required");
        this.reason = validateReason(reason);
        this.status = ReportStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public static ReportedMessage create(Message message, UUID reporterId, String reason) {
        return new ReportedMessage(message, reporterId, reason);
    }

    /**
     * Records the moderator resolution. Only PENDING reports can be resolved,
     * and only into a terminal status (REVIEWED, DISMISSED or ACTIONED).
     */
    public void resolve(ReportStatus resolutionStatus, String resolutionNote) {
        if (status != ReportStatus.PENDING) {
            throw new InvalidChatOperationException("Report is already resolved: " + id);
        }
        Objects.requireNonNull(resolutionStatus, "resolutionStatus is required");
        if (!EnumSet.of(ReportStatus.REVIEWED, ReportStatus.DISMISSED, ReportStatus.ACTIONED).contains(resolutionStatus)) {
            throw new InvalidChatOperationException("Invalid resolution status: " + resolutionStatus);
        }
        this.status = resolutionStatus;
        this.resolution = resolutionNote;
        this.reviewedAt = Instant.now();
    }

    public UUID getMessageId() {
        return message.getId();
    }

    private static String validateReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new InvalidChatOperationException("Report reason must not be blank");
        }
        if (reason.length() > MAX_REASON_LENGTH) {
            throw new InvalidChatOperationException("Reason exceeds " + MAX_REASON_LENGTH + " characters");
        }
        return reason;
    }
}
