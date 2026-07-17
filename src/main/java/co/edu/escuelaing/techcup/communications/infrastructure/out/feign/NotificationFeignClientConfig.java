package co.edu.escuelaing.techcup.communications.infrastructure.out.feign;

import co.edu.escuelaing.techcup.communications.infrastructure.config.NotificationServiceProperties;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

/**
 * Not annotated with {@code @Configuration} on purpose: Spring Cloud OpenFeign instantiates
 * classes passed via {@code @FeignClient(configuration = ...)} into a client-scoped context
 * regardless of the annotation. Adding {@code @Configuration} here would let the main
 * application's component scan pick it up too, turning this notification-only interceptor into a
 * bean shared by every Feign client (same reasoning as {@link GroqFeignClientConfig}).
 */
class NotificationFeignClientConfig {

    static final String API_KEY_HEADER = "X-Internal-Api-Key";

    @Bean
    RequestInterceptor notificationApiKeyInterceptor(NotificationServiceProperties properties) {
        return template -> template.header(API_KEY_HEADER, properties.apiKey());
    }
}
