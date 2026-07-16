package co.edu.escuelaing.techcup.communications.infrastructure.out.feign;

import co.edu.escuelaing.techcup.communications.domain.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.AuditServiceClient;
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
    public void recordEvent(String eventType, UUID entityId, String detail) {
        try {
            feignClient.recordEvent(new AuditPayload(eventType, entityId, detail, Instant.now()));
        } catch (FeignException ex) {
            throw new IntegrationException(SERVICE, ex);
        }
    }
}
