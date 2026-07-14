package co.edu.escuelaing.techcup.communications.service.client.feign;

import co.edu.escuelaing.techcup.communications.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.service.client.AuditServiceClient;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class FeignAuditServiceClient implements AuditServiceClient {

    private static final String SERVICE = "audit service";

    private final AuditServiceFeignClient feignClient;

    public FeignAuditServiceClient(AuditServiceFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    @Override
    public void record(String eventType, UUID entityId, String detail) {
        try {
            feignClient.recordEvent(new AuditPayload(eventType, entityId, detail, Instant.now()));
        } catch (FeignException ex) {
            throw new IntegrationException(SERVICE, ex);
        }
    }
}
