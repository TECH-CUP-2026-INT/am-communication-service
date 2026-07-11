package co.edu.escuelaing.techcup.communications.service.client.rest;

import java.time.Instant;
import java.util.UUID;

record AuditPayload(String eventType, UUID entityId, String detail, Instant occurredAt) {
}
