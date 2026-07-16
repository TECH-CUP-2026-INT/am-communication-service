package co.edu.escuelaing.techcup.communications.domain.service.ports.out;

import java.util.UUID;

/** Outbound port to the Auditing microservice. */
public interface AuditServiceClient {

    void recordEvent(String eventType, UUID entityId, String detail);
}
