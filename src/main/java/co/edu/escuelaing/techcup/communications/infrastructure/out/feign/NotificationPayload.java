package co.edu.escuelaing.techcup.communications.infrastructure.out.feign;

import java.util.UUID;

record NotificationPayload(UUID recipientId, String title, String message) {
}
