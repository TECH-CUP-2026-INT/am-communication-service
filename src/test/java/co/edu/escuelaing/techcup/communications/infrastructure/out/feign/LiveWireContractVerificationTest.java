package co.edu.escuelaing.techcup.communications.infrastructure.out.feign;

import co.edu.escuelaing.techcup.communications.infrastructure.config.IntegrationProperties;
import co.edu.escuelaing.techcup.communications.infrastructure.config.NotificationServiceProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boots the real {@code Feign*Client} adapters through Spring Cloud OpenFeign's actual wiring
 * (real {@code SpringMvcContract}, real Jackson en/decoders, real {@link NotificationFeignClientConfig}
 * interceptor) against plain JDK {@link HttpServer} stubs that mimic the exact response shapes
 * verified against cc-users-players-service, cc-teams-service, and am-notification-service's
 * current source (2026-07-17) — proving the request path/method/headers/body this service actually
 * sends on the wire match what those services actually expect, not just that our code compiles
 * against our own assumptions about their contract.
 *
 * <p>No datasource is needed for any of this, so JPA/DataSource autoconfiguration is excluded
 * instead of requiring a real Postgres for what is otherwise a pure HTTP-layer check.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = LiveWireContractVerificationTest.TestConfig.class)
class LiveWireContractVerificationTest {

    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            JpaRepositoriesAutoConfiguration.class
    })
    @EnableFeignClients(clients = {UserServiceFeignClient.class, TeamServiceFeignClient.class, NotificationServiceFeignClient.class})
    @EnableConfigurationProperties({IntegrationProperties.class, NotificationServiceProperties.class})
    @Import({FeignUserServiceClient.class, FeignTeamServiceClient.class, FeignNotificationServiceClient.class})
    @Configuration
    static class TestConfig {
    }

    private static HttpServer playersServer;
    private static HttpServer teamsServer;
    private static HttpServer notificationServer;

    private static final UUID EXISTING_PLAYER = UUID.randomUUID();
    private static final UUID UNKNOWN_PLAYER = UUID.randomUUID();
    private static final UUID EXISTING_TEAM = UUID.randomUUID();
    private static final UUID UNKNOWN_TEAM = UUID.randomUUID();
    private static final String VALID_API_KEY = "test-internal-key";

    private static final AtomicReference<String> lastPlayersPath = new AtomicReference<>();
    private static final AtomicReference<String> lastNotificationBody = new AtomicReference<>();
    private static final AtomicReference<String> lastNotificationApiKeyHeader = new AtomicReference<>();

    @AfterAll
    static void stopStubs() {
        playersServer.stop(0);
        teamsServer.stop(0);
        notificationServer.stop(0);
    }

    @DynamicPropertySource
    static void wireStubs(DynamicPropertyRegistry registry) throws IOException {
        // Mirrors cc-users-players-service's InternalPlayerController: context-path /api/v1,
        // GET /internal/players/{id}/exists, always 200 with {"exists": boolean} — never 404.
        playersServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        playersServer.createContext("/api/v1/internal/players/", exchange -> {
            lastPlayersPath.set(exchange.getRequestURI().getPath());
            boolean exists = exchange.getRequestURI().getPath().contains(EXISTING_PLAYER.toString());
            byte[] body = ("{\"exists\":" + exists + "}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        playersServer.start();

        // Mirrors cc-teams-service's TeamInfoController: GET /teams/{id}, 200 if it exists
        // (body ignored by our client), 404 if not.
        teamsServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        teamsServer.createContext("/teams/", exchange -> {
            boolean exists = exchange.getRequestURI().getPath().contains(EXISTING_TEAM.toString());
            if (exists) {
                byte[] body = "{\"teamName\":\"Astro Merge\",\"rosterSize\":5}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body);
                }
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        });
        teamsServer.start();

        // Mirrors am-notification-service's ChatEventController: POST /api/notificaciones/mensajes,
        // guarded by SecurityConfig's InternalApiKeyFilter (X-Internal-Api-Key header, role
        // SERVICIO_INTERNO) — 401 without a matching key, 202 with one.
        notificationServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        notificationServer.createContext("/api/notificaciones/mensajes", exchange -> {
            String apiKey = exchange.getRequestHeaders().getFirst("X-Internal-Api-Key");
            lastNotificationApiKeyHeader.set(apiKey);
            byte[] requestBody = exchange.getRequestBody().readAllBytes();
            lastNotificationBody.set(new String(requestBody, StandardCharsets.UTF_8));
            if (VALID_API_KEY.equals(apiKey)) {
                exchange.sendResponseHeaders(202, -1);
            } else {
                exchange.sendResponseHeaders(401, -1);
            }
        });
        notificationServer.start();

        registry.add("integrations.user-service.base-url",
                () -> "http://127.0.0.1:" + playersServer.getAddress().getPort() + "/api/v1");
        registry.add("integrations.user-service.existence-check-enabled", () -> "true");
        registry.add("integrations.team-service.base-url",
                () -> "http://127.0.0.1:" + teamsServer.getAddress().getPort());
        registry.add("integrations.team-service.existence-check-enabled", () -> "true");
        registry.add("integrations.notification-service.base-url",
                () -> "http://127.0.0.1:" + notificationServer.getAddress().getPort());
        registry.add("integrations.notification-service.api-key", () -> VALID_API_KEY);
    }

    @Autowired
    private FeignUserServiceClient userServiceClient;

    @Autowired
    private FeignTeamServiceClient teamServiceClient;

    @Autowired
    private FeignNotificationServiceClient notificationServiceClient;

    @Test
    void userExistenceCheckHitsTheRealPathAndParsesTheBooleanBody() {
        assertThat(userServiceClient.exists(EXISTING_PLAYER)).isTrue();
        assertThat(userServiceClient.exists(UNKNOWN_PLAYER)).isFalse();
        assertThat(lastPlayersPath.get()).isEqualTo("/api/v1/internal/players/" + UNKNOWN_PLAYER + "/exists");
    }

    @Test
    void teamExistenceCheckTreats200AsExistingAnd404AsMissing() {
        assertThat(teamServiceClient.exists(EXISTING_TEAM)).isTrue();
        assertThat(teamServiceClient.exists(UNKNOWN_TEAM)).isFalse();
    }

    @Test
    void notificationCallSendsTheInternalApiKeyAndTheChatMessageEventShape() {
        UUID chatId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();

        notificationServiceClient.notify(chatId, recipientId, "ESCALATED: FAQ -> CHATBOT");

        assertThat(lastNotificationApiKeyHeader.get()).isEqualTo(VALID_API_KEY);
        String body = lastNotificationBody.get();
        assertThat(body).contains("\"chatId\":\"" + chatId + "\"");
        assertThat(body).contains("\"recipientId\":\"" + recipientId + "\"");
        assertThat(body).contains("\"messagePreview\":\"ESCALATED: FAQ -> CHATBOT\"");
        assertThat(body).contains("\"senderId\"");
        assertThat(body).contains("\"senderName\"");
        assertThat(body).contains("\"sentAt\"");
    }
}
