package co.edu.escuelaing.techcup.communications.service.client.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "audit-service", url = "${integrations.audit-service.base-url}")
interface AuditServiceFeignClient {

    @PostMapping("/audit-events")
    void recordEvent(@RequestBody AuditPayload payload);
}
