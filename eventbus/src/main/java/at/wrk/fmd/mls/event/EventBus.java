package at.wrk.fmd.mls.event;

/**
 * This class is used for publishing events and managing the handlers
 */
public interface EventBus {

    /**
     * Publish an event to all matching handlers
     *
     * @param event The event data
     * @param <E> The type of the event
     */
    <E extends Event> void publish(E event);

    /**
     * Register an event handler
     *
     * @param handler The handler instance
     */
    void registerHandler(EventHandler<?> handler);

    /**
     * Unregister an event handler
     *
     * @param handler The handler instance
     */
    void unregisterHandler(EventHandler<?> handler);
}
