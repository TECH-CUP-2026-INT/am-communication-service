package co.edu.escuelaing.techcup.communications.service.client.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "${integrations.notification-service.base-url}")
interface NotificationServiceFeignClient {

    @PostMapping("/notifications")
    void sendNotification(@RequestBody NotificationPayload payload);
}
