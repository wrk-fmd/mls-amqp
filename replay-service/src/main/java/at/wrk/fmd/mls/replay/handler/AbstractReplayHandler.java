package at.wrk.fmd.mls.replay.handler;

import static java.util.Objects.requireNonNull;

import at.wrk.fmd.mls.event.Event;
import at.wrk.fmd.mls.event.EventHandler;
import at.wrk.fmd.mls.replay.dto.ReplayStartDto;
import at.wrk.fmd.mls.replay.message.NotificationMessage;
import at.wrk.fmd.mls.replay.message.ReplayEvent;
import at.wrk.fmd.mls.replay.message.ReplayRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This is the basis for a worker which sends updates and replays initial data to the message broker
 */
public abstract class AbstractReplayHandler<T> implements EventHandler<ReplayEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final AmqpTemplate amqpTemplate;
    private final String target;

    /**
     * @param amqpTemplate The injected AmqpTemplate to use
     * @param target The target exchange that is handled by this worker
     */
    public AbstractReplayHandler(final AmqpTemplate amqpTemplate, final String target) {
        this.amqpTemplate = requireNonNull(amqpTemplate, "AmqpTemplate must not be null");
        this.target = requireNonNull(target, "Target must not be null");
    }

    @Override
    public void handle(final ReplayEvent event) {
        if (event instanceof NotificationMessage) {
            this.handle((NotificationMessage) event);
        } else if (event instanceof ReplayRequest) {
            this.handle((ReplayRequest) event);
        } else {
            LOG.warn("Received unknown replay event {}", event);
        }
    }

    @Override
    public Class<ReplayEvent> type() {
        return ReplayEvent.class;
    }

    @Override
    public boolean canHandle(final Event event) {
        return event instanceof ReplayEvent && target.equals(((ReplayEvent) event).getTarget());
    }

    private void handle(final NotificationMessage message) {
        String routingKey = buildRoutingKey(message.getRoutingKey());

        LOG.debug("Sending update to '{}' with key '{}': {}", message.getTarget(), routingKey, message.getPayload());
        amqpTemplate.convertAndSend(message.getTarget(), routingKey, message.getPayload());
    }

    private void handle(final ReplayRequest request) {
        LOG.debug("Replaying messages for '{}' with key '{}' to '{}'", request.getTarget(), request.getKey(), request.getRecipient());
        amqpTemplate.convertAndSend(request.getRecipient(), new ReplayStartDto(request.getKey()));
        getData(request.getKey()).forEach(item -> amqpTemplate.convertAndSend(request.getRecipient(), item));
    }

    private String buildRoutingKey(final Object key) {
        if (key == null) {
            return null;
        }
        if (key instanceof Collection) {
            // Multiple keys are concatenated by dots
            return ((Collection<?>) key).stream().map(Object::toString).collect(Collectors.joining("."));
        }
        return key.toString();
    }

    /**
     * Helper method for extracting an id from a routing key
     *
     * @param key The dot-separated routing key
     * @param index The zero-based index of the id to extract
     * @return The id, or null if no number was found for the given index
     */
    protected Long parseIdFromKey(String key, int index) {
        if (key == null || key.isBlank()) {
            LOG.warn("Received empty routing key, could not parse id at index {}", index);
            return null;
        }

        String[] parts = key.trim().split("\\.");
        if (parts.length <= index) {
            LOG.warn("Routing key '{}' does not contain id at index {}", key, index);
            return null;
        }

        try {
            return Long.parseLong(parts[index]);
        } catch (NumberFormatException e) {
            LOG.warn("Could not parse id at index {} from routing key '{}'", key, index);
            return null;
        }
    }

    /**
     * Load the data that should be replayed
     *
     * @param key The requested routing key
     * @return A collection of messages to send
     */
    protected abstract Collection<T> getData(String key);
}
