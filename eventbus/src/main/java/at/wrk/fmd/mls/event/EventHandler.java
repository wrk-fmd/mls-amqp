package at.wrk.fmd.mls.event;

/**
 * Classes implementing this interface can handle specific event types
 *
 * @param <E> The class of events handled by this handler
 */
public interface EventHandler<E extends Event> {

    /**
     * Execute the event handling logic
     *
     * @param event The event data
     * @throws InvalidEventException Thrown if an event was considered invalid and should not be resubmitted
     * @throws RuntimeException Any other exception, which should be considered as temporary, i.e. the calling code may resubmit the event
     */
    void handle(E event);

    /**
     * Get the event type handled by this handler
     *
     * @return The class of the handled events
     */
    Class<E> type();

    /**
     * Check if a specific event can be handled by this handler
     * By default, all events matching the type of the handler are allowed, but implementations can check additional conditions
     *
     * @param event The event data
     * @return True iff the event can be handled
     */
    default boolean canHandle(E event) {
        return true;
    }
}
