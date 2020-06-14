package at.wrk.fmd.mls.event.impl;

import at.wrk.fmd.mls.event.Event;
import at.wrk.fmd.mls.event.EventBus;
import at.wrk.fmd.mls.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * This event bus implementation wraps each handler in a worker which dispatches events using a blocking queue
 */
@Component
class WorkerEventBus implements EventBus {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Map<EventHandler<?>, WorkerEventHandler<?>> workers = new LinkedHashMap<>();
    private final Executor executor = Executors.newCachedThreadPool();

    @Override
    public <E extends Event> void publish(final E event) {
        @SuppressWarnings("unchecked")
        long matched = workers.values().stream()
                .filter(h -> h.type().isInstance(event))
                .map(h -> (EventHandler<E>) h)
                .filter(h -> h.canHandle(event))
                .peek(h -> h.handle(event))
                .count();
        if (matched > 0) {
            LOG.debug("Dispatched event {} to {} handlers", event.getClass(), matched);
        } else {
            LOG.warn("Received unhandled event {}", event.getClass());
        }
    }

    @Override
    public void registerHandler(final EventHandler<?> handler) {
        this.workers.computeIfAbsent(handler, this::createWorker);
    }

    @Override
    public void unregisterHandler(final EventHandler<?> handler) {
        WorkerEventHandler<?> worker = workers.remove(handler);
        if (worker != null) {
            worker.close();
        }
    }

    private <E extends Event> WorkerEventHandler<E> createWorker(final EventHandler<E> handler) {
        WorkerEventHandler<E> workerHandler = new WorkerEventHandler<>(handler);
        executor.execute(workerHandler);
        return workerHandler;
    }
}
