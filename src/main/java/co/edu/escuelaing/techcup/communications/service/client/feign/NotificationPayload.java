package co.edu.escuelaing.techcup.communications.service.client.feign;

import java.util.UUID;

record NotificationPayload(UUID recipientId, String title, String message) {
}
