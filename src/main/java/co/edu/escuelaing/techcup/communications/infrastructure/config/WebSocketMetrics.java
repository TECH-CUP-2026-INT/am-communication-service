package co.edu.escuelaing.techcup.communications.infrastructure.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * STOMP metrics, mirroring what a raw {@code TextWebSocketHandler} would track by hand
 * (active sessions, messages in/out) plus two extra counters for the security guards this
 * service adds on top: rejected CONNECTs and denied SUBSCRIBEs.
 */
@Slf4j
@Component
public class WebSocketMetrics {

    private final AtomicInteger activeSessions = new AtomicInteger();
    private final Counter chatMessagesReceived;
    private final Counter supportMessagesReceived;
    private final Counter messagesBroadcast;
    private final Counter authFailures;
    private final Counter subscriptionsDenied;

    public WebSocketMetrics(MeterRegistry registry) {
        registry.gauge("ws.sessions.active", activeSessions);
        this.chatMessagesReceived = Counter.builder("ws.messages.received").tag("kind", "chat").register(registry);
        this.supportMessagesReceived = Counter.builder("ws.messages.received").tag("kind", "support").register(registry);
        this.messagesBroadcast = Counter.builder("ws.messages.broadcast").register(registry);
        this.authFailures = Counter.builder("ws.auth.failures").register(registry);
        this.subscriptionsDenied = Counter.builder("ws.subscriptions.denied").register(registry);
    }

    @EventListener
    public void onSessionConnected(SessionConnectedEvent event) {
        int active = activeSessions.incrementAndGet();
        log.info("WS_CONNECTED", StructuredArguments.kv("active_sessions", active));
    }

    @EventListener
    public void onSessionDisconnected(SessionDisconnectEvent event) {
        int active = activeSessions.updateAndGet(current -> Math.max(0, current - 1));
        log.info("WS_DISCONNECTED", StructuredArguments.kv("active_sessions", active));
    }

    public void recordChatMessageReceived() {
        chatMessagesReceived.increment();
    }

    public void recordSupportMessageReceived() {
        supportMessagesReceived.increment();
    }

    public void recordBroadcast() {
        messagesBroadcast.increment();
    }

    public void recordAuthFailure() {
        authFailures.increment();
        log.warn("WS_AUTH_FAILED");
    }

    public void recordSubscriptionDenied(String destination) {
        subscriptionsDenied.increment();
        log.warn("WS_SUBSCRIBE_DENIED", StructuredArguments.kv("destination", destination));
    }
}
