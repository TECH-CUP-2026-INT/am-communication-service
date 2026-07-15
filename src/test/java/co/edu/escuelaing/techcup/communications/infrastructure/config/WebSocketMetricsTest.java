package co.edu.escuelaing.techcup.communications.infrastructure.config;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import static org.assertj.core.api.Assertions.assertThat;

class WebSocketMetricsTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final WebSocketMetrics metrics = new WebSocketMetrics(registry);

    private static Message<byte[]> emptyMessage() {
        return MessageBuilder.withPayload(new byte[0]).build();
    }

    private double activeSessions() {
        return registry.get("ws.sessions.active").gauge().value();
    }

    @Test
    void tracksActiveSessionsAcrossConnectAndDisconnect() {
        assertThat(activeSessions()).isZero();

        metrics.onSessionConnected(new SessionConnectedEvent(this, emptyMessage()));
        metrics.onSessionConnected(new SessionConnectedEvent(this, emptyMessage()));
        assertThat(activeSessions()).isEqualTo(2);

        metrics.onSessionDisconnected(
                new SessionDisconnectEvent(this, emptyMessage(), "session-1", CloseStatus.NORMAL));
        assertThat(activeSessions()).isEqualTo(1);
    }

    @Test
    void neverGoesBelowZeroActiveSessions() {
        metrics.onSessionDisconnected(
                new SessionDisconnectEvent(this, emptyMessage(), "session-1", CloseStatus.NORMAL));

        assertThat(activeSessions()).isZero();
    }

    @Test
    void countsReceivedAndBroadcastMessagesByKind() {
        metrics.recordChatMessageReceived();
        metrics.recordSupportMessageReceived();
        metrics.recordSupportMessageReceived();
        metrics.recordBroadcast();

        assertThat(registry.get("ws.messages.received").tag("kind", "chat").counter().count()).isEqualTo(1);
        assertThat(registry.get("ws.messages.received").tag("kind", "support").counter().count()).isEqualTo(2);
        assertThat(registry.get("ws.messages.broadcast").counter().count()).isEqualTo(1);
    }

    @Test
    void countsAuthFailuresAndDeniedSubscriptions() {
        metrics.recordAuthFailure();
        metrics.recordSubscriptionDenied("/topic/chat/stranger");
        metrics.recordSubscriptionDenied("/topic/chat/another");

        assertThat(registry.get("ws.auth.failures").counter().count()).isEqualTo(1);
        assertThat(registry.get("ws.subscriptions.denied").counter().count()).isEqualTo(2);
    }
}
