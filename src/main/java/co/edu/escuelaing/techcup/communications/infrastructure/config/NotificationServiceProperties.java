package co.edu.escuelaing.techcup.communications.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param apiKey shared internal secret am-notification-service requires (header
 *               {@code X-Internal-Api-Key}) on its service-to-service webhooks, including the
 *               chat-message notification this service posts to. No default: an unset key just
 *               means every notification call gets a {@code 401}, logged and swallowed the same
 *               way any other notification failure is — this integration is best-effort, so it
 *               doesn't need the fail-fast treatment {@code JWT_SECRET}/{@code GROQ_API_KEY} get.
 */
@ConfigurationProperties(prefix = "integrations.notification-service")
public record NotificationServiceProperties(String apiKey) {
}
