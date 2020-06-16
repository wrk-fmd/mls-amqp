package at.wrk.fmd.mls.stomp.config;

import static org.springframework.messaging.simp.SimpMessageType.CONNECT;
import static org.springframework.messaging.simp.SimpMessageType.DISCONNECT;
import static org.springframework.messaging.simp.SimpMessageType.HEARTBEAT;
import static org.springframework.messaging.simp.SimpMessageType.UNSUBSCRIBE;

import org.springframework.messaging.Message;
import org.springframework.security.access.expression.AbstractSecurityExpressionHandler;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.security.messaging.access.expression.DefaultMessageSecurityExpressionHandler;

/**
 * This class provides the basis for Socket Authorization config and should be implemented by the actual applications
 */
public abstract class StompSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }

    @Override
    protected final void configureInbound(MessageSecurityMetadataSourceRegistry registry) {
        // Set a customized expression handler
        DefaultMessageSecurityExpressionHandler<Object> expressionHandler = new DefaultMessageSecurityExpressionHandler<>();
        customizeExpressionHandler(expressionHandler);
        registry = registry.expressionHandler(expressionHandler);

        // Allow some basic access
        registry = registry
                .simpTypeMatchers(CONNECT).authenticated()
                .simpTypeMatchers(UNSUBSCRIBE, DISCONNECT, HEARTBEAT).permitAll();

        // Add application specific checks
        registry = customizeInbound(registry);

        // Deny everything else
        registry.anyMessage().denyAll();
    }

    protected abstract void customizeExpressionHandler(AbstractSecurityExpressionHandler<Message<Object>> handler);

    protected abstract MessageSecurityMetadataSourceRegistry customizeInbound(MessageSecurityMetadataSourceRegistry registry);

    protected String buildDestination(String exchange, String key) {
        return String.format("/exchange/%s/%s", exchange, key);
    }
}
