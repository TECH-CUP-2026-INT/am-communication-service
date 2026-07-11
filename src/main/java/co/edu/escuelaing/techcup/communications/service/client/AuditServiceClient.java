package co.edu.escuelaing.techcup.communications.service.client;

import java.util.UUID;

/** Outbound port to the Auditing microservice. */
public interface AuditServiceClient {

    void record(String eventType, UUID entityId, String detail);
}
