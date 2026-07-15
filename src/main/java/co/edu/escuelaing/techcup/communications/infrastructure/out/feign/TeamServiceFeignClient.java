package co.edu.escuelaing.techcup.communications.infrastructure.out.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "team-service", url = "${integrations.team-service.base-url}")
interface TeamServiceFeignClient {

    @GetMapping("/teams/{id}")
    void getTeam(@PathVariable("id") UUID id);
}
