package at.wrk.fmd.mls.replay.message;

/**
 * This class represents a notification message to be sent to clients
 */
public class NotificationMessage implements ReplayEvent {

    private final String target;
    private final Object routingKey;
    private final Object payload;

    /**
     * Send a notification to an exchange without a routing key
     *
     * @param target The name of the target exchange
     * @param payload The payload data
     */
    public NotificationMessage(String target, Object payload) {
        this(target, null, payload);
    }

    /**
     * Send a notification to an exchange target
     *
     * @param target The name of the target exchange
     * @param routingKey The (optional) routing key for the message
     * @param payload The payload data
     */
    public NotificationMessage(String target, Object routingKey, Object payload) {
        this.target = target;
        this.routingKey = routingKey;
        this.payload = payload;
    }

    /**
     * @return The name of the target exchange
     */
    @Override
    public String getTarget() {
        return target;
    }

    /**
     * @return The routing key for the message
     */
    public Object getRoutingKey() {
        return routingKey;
    }

    /**
     * @return The payload data
     */
    public Object getPayload() {
        return payload;
    }
}
