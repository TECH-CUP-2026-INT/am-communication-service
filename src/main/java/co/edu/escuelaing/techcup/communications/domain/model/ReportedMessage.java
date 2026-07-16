package co.edu.escuelaing.techcup.communications.domain.model;

import co.edu.escuelaing.techcup.communications.domain.model.enums.ReportStatus;
import co.edu.escuelaing.techcup.communications.domain.exception.InvalidChatOperationException;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class ReportedMessage {

    public static final int MAX_REASON_LENGTH = 500;

    private UUID id;
    private Message message;
    private UUID reporterId;
    private String reason;
    private ReportStatus status;
    private String resolution;
    private Instant createdAt;
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

    // Reconstruction from a persisted row: every column maps to one argument, so the parameter
    // count mirrors the stored state and a builder would only add indirection here.
    @SuppressWarnings("java:S107")
    public static ReportedMessage fromPersistence(UUID id, Message message, UUID reporterId, String reason,
                                                  ReportStatus status, String resolution,
                                                  Instant createdAt, Instant reviewedAt) {
        ReportedMessage report = new ReportedMessage();
        report.id = id;
        report.message = message;
        report.reporterId = reporterId;
        report.reason = reason;
        report.status = status;
        report.resolution = resolution;
        report.createdAt = createdAt;
        report.reviewedAt = reviewedAt;
        return report;
    }


    public void resolve(ReportStatus resolutionStatus, String resolutionNote) {
        if (status != ReportStatus.PENDING) {
            throw new InvalidChatOperationException("El reporte ya fue resuelto: " + id);
        }
        Objects.requireNonNull(resolutionStatus, "resolutionStatus is required");
        if (!EnumSet.of(ReportStatus.REVIEWED, ReportStatus.DISMISSED, ReportStatus.ACTIONED).contains(resolutionStatus)) {
            throw new InvalidChatOperationException("Estado de resolución inválido: " + resolutionStatus);
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
            throw new InvalidChatOperationException("El motivo del reporte no puede estar vacío");
        }
        if (reason.length() > MAX_REASON_LENGTH) {
            throw new InvalidChatOperationException("El motivo supera los " + MAX_REASON_LENGTH + " caracteres");
        }
        TextValidation.rejectControlCharacters(reason, "El motivo del reporte");
        return reason;
    }
}
