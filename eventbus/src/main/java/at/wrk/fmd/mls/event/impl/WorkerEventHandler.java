package at.wrk.fmd.mls.event.impl;

import at.wrk.fmd.mls.event.Event;
import at.wrk.fmd.mls.event.EventHandler;
import at.wrk.fmd.mls.event.InvalidEventException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * This is a wrapper for event handlers using a queue for dispatching
 *
 * @param <E> The event class handled by the handler
 */
class WorkerEventHandler<E extends Event> implements EventHandler<E>, Runnable, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final BlockingDeque<E> queue = new LinkedBlockingDeque<>();
    private final EventHandler<E> delegate;
    private final E poisonPill;

    public WorkerEventHandler(final EventHandler<E> delegate) {
        this.poisonPill = createPoisonPill(delegate.type());
        this.delegate = delegate;
    }

    @Override
    public void handle(final E event) {
        queue.add(event);
    }

    @Override
    public Class<E> type() {
        return delegate.type();
    }

    @Override
    public boolean canHandle(final Event event) {
        return delegate.canHandle(event);
    }

    @Override
    public void run() {
        LOG.info("Worker for {} started running", delegate.getClass());

        while (true) {
            E event = null;
            try {
                event = queue.take();
                if (event == poisonPill) {
                    LOG.info("Received poison pill, stopping worker for {}", delegate.getClass());
                    break;
                }

                // Just run the event (synchronously)
                LOG.debug("Dispatching event to {}, {} remaining in queue", delegate.getClass(), queue.size());
                delegate.handle(event);
            } catch (InvalidEventException e) {
                // Handler rejected event permanently, do not resubmit
                LOG.warn("InvalidEventException for event {} in handler {}", event, delegate.getClass(), e);
            } catch (Exception e) {
                if (event == null) {
                    // Exception happened before an event was received, nothing to do
                    LOG.error("Exception on waiting for event for {}", delegate.getClass(), e);
                } else {
                    // Event was received, but some exception occurred in the handler: Add at beginning of queue and try again
                    LOG.error("Exception on handling event {} in handler {}, adding back to queue", event, delegate.getClass(), e);
                    queue.addFirst(event);
                }
            }
        }
    }

    @Override
    public void close() {
        // This will just add a poison pill to the queue, causing the worker to stop
        // Events added before this method was called will still be dispatched
        queue.add(poisonPill);
    }

    private E createPoisonPill(final Class<E> type) {
        // Use reflection to build an empty event instance which can be used as poison pill
        // TODO This is not optimal, maybe just pass in an external instance?

        if (type.isInterface()) {
            // Create a proxy for the interface
            @SuppressWarnings("unchecked")
            E poison = (E) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, (o, method, objects) -> {
                throw new UnsupportedOperationException();
            });
            return poison;
        }

        try {
            // TODO This will not work if type is abstract
            Constructor<E> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            //
            LOG.error("Failed to create a poison pill for the WorkerEventHandler for {}", type, e);
            throw new RuntimeException(e);
        }
    }
}
