package co.edu.escuelaing.techcup.communications.service.client;

import java.util.UUID;

/**
 * Outbound port to the Auditing Service. A logging fallback is provided until the
 * REST adapter is wired; the real adapter replaces it via @ConditionalOnMissingBean.
 */
public interface AuditServiceClient {

    void record(String eventType, UUID entityId, String detail);
}
