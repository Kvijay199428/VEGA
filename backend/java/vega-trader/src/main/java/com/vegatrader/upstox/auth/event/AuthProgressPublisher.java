package com.vegatrader.upstox.auth.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Auth Progress Publisher
 * 
 * Publishes real-time token generation progress events via Server-Sent Events
 * (SSE).
 * Uses Project Reactor's Sinks for thread-safe event emission.
 * 
 * Frontend subscribes to the stream to display live progress during batch
 * authentication.
 * 
 * @since Production Auth Architecture v2.0
 */
@Component
public class AuthProgressPublisher {

    private static final Logger logger = LoggerFactory.getLogger(AuthProgressPublisher.class);

    private final Sinks.Many<AuthProgressEvent> sink = Sinks.many().multicast().onBackpressureBuffer();

    /**
     * Emit a progress event to all subscribers.
     * 
     * @param event The progress event to publish
     */
    public void emit(AuthProgressEvent event) {
        logger.debug("[SSE-PROGRESS] Emitting: {} | {}", event.getApi(), event.getStatus());

        Sinks.EmitResult result = sink.tryEmitNext(event);

        if (result.isFailure()) {
            logger.warn("[SSE-PROGRESS] Failed to emit event: {} - {}", event.getApi(), result);
        }
    }

    /**
     * Get the event stream for subscription.
     * 
     * @return Flux of auth progress events
     */
    public Flux<AuthProgressEvent> getStream() {
        return sink.asFlux();
    }
}
