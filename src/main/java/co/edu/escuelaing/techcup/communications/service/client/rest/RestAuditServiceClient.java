package co.edu.escuelaing.techcup.communications.service.client.rest;

import co.edu.escuelaing.techcup.communications.config.IntegrationProperties;
import co.edu.escuelaing.techcup.communications.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.service.client.AuditServiceClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.UUID;

@Component
public class RestAuditServiceClient implements AuditServiceClient {

    private static final String SERVICE = "audit service";

    private final RestClient restClient;

    public RestAuditServiceClient(RestClient.Builder builder, IntegrationProperties properties) {
        this.restClient = builder.baseUrl(properties.auditService().baseUrl()).build();
    }

    @Override
    public void record(String eventType, UUID entityId, String detail) {
        try {
            restClient.post()
                    .uri("/audit-events")
                    .body(new AuditPayload(eventType, entityId, detail, Instant.now()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new IntegrationException(SERVICE, ex);
        }
    }
}
