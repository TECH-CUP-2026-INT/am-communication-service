package co.edu.escuelaing.techcup.communications.infrastructure.out.feign;

import java.time.Instant;
import java.util.UUID;

/**
 * Mirrors am-notification-service's {@code ChatMessageEvent} record field-for-field
 * ({@code POST /api/notificaciones/mensajes}) — their own code marks this contract as proposed
 * and pending confirmation with this service's team.
 */
record ChatMessageEvent(
        UUID chatId,
        UUID senderId,
        String senderName,
        UUID recipientId,
        String messagePreview,
        Instant sentAt
) {
}
