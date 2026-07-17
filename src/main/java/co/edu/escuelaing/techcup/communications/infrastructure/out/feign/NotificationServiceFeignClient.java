package co.edu.escuelaing.techcup.communications.infrastructure.out.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "notification-service",
        url = "${integrations.notification-service.base-url}",
        configuration = NotificationFeignClientConfig.class)
interface NotificationServiceFeignClient {

    @PostMapping("/api/notificaciones/mensajes")
    void sendNotification(@RequestBody ChatMessageEvent event);
}
