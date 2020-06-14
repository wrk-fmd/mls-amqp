package at.wrk.fmd.mls.stomp.fowarder;

import static at.wrk.fmd.mls.amqp.ReplayConstants.REPLAY_TRIGGER_EXCHANGE;
import static at.wrk.fmd.mls.amqp.ReplayConstants.ROUTING_KEY_HEADER;
import static java.util.Objects.requireNonNull;

import at.wrk.fmd.mls.amqp.event.ReplayRequest;
import at.wrk.fmd.mls.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

/**
 * This class forwards replay requests to the actual microservice
 */
@Component
public class ReplayRequestForwarder implements EventHandler<ReplayRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final AmqpTemplate amqpTemplate;

    @Autowired
    public ReplayRequestForwarder(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = requireNonNull(amqpTemplate, "AmqpTemplate must not be null");
    }

    @Override
    public void handle(final ReplayRequest request) {
        // Send a null-message (as JSON), using headers for the real information
        LOG.debug("Forwarding replay request of {}/{} for {}", request.getTarget(), request.getKey(), request.getRecipient());
        amqpTemplate.convertAndSend(REPLAY_TRIGGER_EXCHANGE, request.getTarget(), "null", m -> {
            MessageProperties p = m.getMessageProperties();
            p.setContentType(MediaType.APPLICATION_JSON_VALUE);
            p.setReplyTo(request.getRecipient());
            p.setHeader(ROUTING_KEY_HEADER, request.getKey());
            return m;
        });
    }

    @Override
    public Class<ReplayRequest> type() {
        return ReplayRequest.class;
    }
}
