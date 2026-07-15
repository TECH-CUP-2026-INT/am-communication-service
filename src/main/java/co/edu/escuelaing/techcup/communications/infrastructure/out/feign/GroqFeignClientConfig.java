package co.edu.escuelaing.techcup.communications.infrastructure.out.feign;

import co.edu.escuelaing.techcup.communications.infrastructure.config.GroqProperties;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

/**
 * Not annotated with {@code @Configuration} on purpose: Spring Cloud OpenFeign instantiates
 * classes passed via {@code @FeignClient(configuration = ...)} into a client-scoped context
 * regardless of the annotation. Adding {@code @Configuration} here would let the main
 * application's component scan (rooted at the same base package) pick it up too, turning the
 * Groq-only auth interceptor into a bean shared by every Feign client.
 */
class GroqFeignClientConfig {

    @Bean
    RequestInterceptor groqAuthInterceptor(GroqProperties properties) {
        return template -> template.header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey());
    }
}
