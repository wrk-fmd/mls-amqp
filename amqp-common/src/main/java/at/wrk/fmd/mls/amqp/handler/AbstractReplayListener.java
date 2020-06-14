package at.wrk.fmd.mls.amqp.handler;

import static at.wrk.fmd.mls.amqp.ReplayConstants.ROUTING_KEY_HEADER;
import static java.util.Objects.requireNonNull;

import at.wrk.fmd.mls.amqp.event.ReplayRequest;
import at.wrk.fmd.mls.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpMessageHeaderAccessor;

import java.lang.invoke.MethodHandles;

/**
 * This is the basis for a ReplayRequest Listener which should be overridden in each component
 * Annotate the overriding class with {@link RabbitListener}, specifying the routing key under which the replay requests are received.
 */
public abstract class AbstractReplayListener {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final EventBus eventBus;
    private final String target;

    /**
     * @param eventBus The injected EventBus to use
     * @param target The target that is handled by this worker
     */
    public AbstractReplayListener(EventBus eventBus, String target) {
        this.eventBus = requireNonNull(eventBus, "EventBus must not be null");
        this.target = requireNonNull(target, "Target must not be null");
    }

    @RabbitHandler
    public void listen(@SuppressWarnings("unused") Object payload, AmqpMessageHeaderAccessor headers) {
        // For some reason Spring does not detect the method if payload is not present as parameter
        String replyTo = headers.getReplyTo();
        String routingKey = (String)headers.getHeader(ROUTING_KEY_HEADER);
        if (replyTo == null) {
            // No return address given, do nothing
            LOG.info("Received replay request without replyTo address");
            return;
        }

        // Trigger the handler for the request
        eventBus.publish(new ReplayRequest(target, replyTo, routingKey));
    }
}
