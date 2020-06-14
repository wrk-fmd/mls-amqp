package at.wrk.fmd.mls.amqp.event;

import at.wrk.fmd.mls.event.Event;

/**
 * This is the base event handled by the replay handler
 */
public interface AmqpEvent extends Event {

    String getTarget();
}
