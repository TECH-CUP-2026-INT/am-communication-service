package co.edu.escuelaing.techcup.communications.service.client.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${integrations.user-service.base-url}")
interface UserServiceFeignClient {

    @GetMapping("/users/{id}")
    void getUser(@PathVariable("id") UUID id);
}
