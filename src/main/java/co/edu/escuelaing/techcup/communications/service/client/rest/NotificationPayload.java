package co.edu.escuelaing.techcup.communications.service.client.rest;

import java.util.UUID;

record NotificationPayload(UUID recipientId, String title, String message) {
}
