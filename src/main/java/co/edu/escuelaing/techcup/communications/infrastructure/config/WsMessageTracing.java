package co.edu.escuelaing.techcup.communications.infrastructure.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

import java.util.Map;

/**
 * Wraps a STOMP {@code @MessageMapping} handler body in a traced span, tagging it, recording
 * errors and always closing it. Shared by {@code ChatWsController} and {@code SupportWsController}
 * so the tracing boilerplate isn't duplicated per handler.
 */
public final class WsMessageTracing {

    private WsMessageTracing() {
    }

    public static void traced(Tracer tracer, String spanName, Map<String, String> tags, Runnable action) {
        Span span = tracer.nextSpan().name(spanName);
        tags.forEach(span::tag);
        span.start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            action.run();
        } catch (RuntimeException ex) {
            span.error(ex);
            throw ex;
        } finally {
            span.end();
        }
    }
}
