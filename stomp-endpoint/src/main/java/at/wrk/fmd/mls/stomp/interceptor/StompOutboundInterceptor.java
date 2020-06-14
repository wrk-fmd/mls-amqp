package at.wrk.fmd.mls.stomp.interceptor;

import static java.util.Objects.requireNonNull;

import at.wrk.fmd.mls.amqp.event.ReplayRequest;
import at.wrk.fmd.mls.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

/**
 * This class intercepts outgoing STOMP frames and modifies them before forwarding them to the client
 */
@Component
public class StompOutboundInterceptor extends AbstractStompInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final EventBus eventBus;

    @Autowired
    public StompOutboundInterceptor(EventBus eventBus) {
        this.eventBus = requireNonNull(eventBus, "EventBus must not be null");
    }

    @Override
    public Message<?> preMessage(Message<?> message, StompHeaderAccessor headers) {
        // Intercept MESSAGE frames
        if (headers.isMutable()) {
            // Remove some Spring/AMQP headers to decrease message size
            headers.removeNativeHeader("__TypeId__");
            headers.removeNativeHeader("redelivered");
            headers.removeNativeHeader("priority");
            headers.removeNativeHeader("persistent");
        }

        return message;
    }

    @Override
    protected Message<?> preReceipt(Message<?> message, StompHeaderAccessor headers) {
        // Trigger message replay after a receipt for the subscription was received from the broker
        String id = headers.getReceiptId();
        if (id == null) {
            // Empty receipt: Do nothing
            LOG.warn("Received empty receipt for {}", headers.getSessionId());
            return null;
        }

        // TODO This would fail if the destination contained colons
        String[] receiptParts = id.split(":", 4);
        if (receiptParts.length < 3 || !receiptParts[0].equals("replay")) {
            // Not a replay receipt, just forward it to the client as is
            return message;
        }

        triggerReplay(receiptParts[1], receiptParts[2]);

        if (receiptParts.length >= 4) {
            // Send receipt to client if requested
            headers.setReceiptId(receiptParts[3]);
            return message;
        }

        return null;
    }

    private void triggerReplay(String destination, String queueName) {
        // Destinations are in the format "/exchanges/topic/[routingKey]"
        String[] destinationParts = destination.split("/");
        if (destinationParts.length < 3) {
            // No destination given, don't start replay
            LOG.debug("Destination {} incomplete, not triggering replay for {}", destination, queueName);
            return;
        }

        // Publish the request to the event bus
        LOG.debug("Triggering replay of {} for {}", destination, queueName);
        String exchangeName = destinationParts[2];
        String routingKey = destinationParts.length >= 4 ? destinationParts[3] : null;
        eventBus.publish(new ReplayRequest(exchangeName, queueName, routingKey));
    }
}
