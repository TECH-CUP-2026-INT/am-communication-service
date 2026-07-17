package co.edu.escuelaing.techcup.communications.infrastructure.out.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${integrations.user-service.base-url}")
interface UserServiceFeignClient {

    // cc-users-players-service's InternalPlayerController: built specifically for
    // service-to-service existence checks. Always returns 200, never 404.
    @GetMapping("/internal/players/{id}/exists")
    ExistsResponse exists(@PathVariable("id") UUID id);
}
