package at.wrk.fmd.mls.replay.message;

import at.wrk.fmd.mls.event.Event;

/**
 * This is the base event handled by the replay handler
 */
public interface ReplayEvent extends Event {

    String getTarget();
}
