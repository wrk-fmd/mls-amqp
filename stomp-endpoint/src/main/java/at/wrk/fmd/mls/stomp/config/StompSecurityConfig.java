package at.wrk.fmd.mls.stomp.config;

import static org.springframework.messaging.simp.SimpMessageType.CONNECT;
import static org.springframework.messaging.simp.SimpMessageType.DISCONNECT;
import static org.springframework.messaging.simp.SimpMessageType.HEARTBEAT;
import static org.springframework.messaging.simp.SimpMessageType.UNSUBSCRIBE;

import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

/**
 * This class provides the basis for Socket Authorization config and should be implemented by the actual applications
 * Implementations should provide a MessageSecurityExpressionHandler bean with a specific PermissionEvaluator
 * to support the hasPermission expression
 */
public abstract class StompSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }

    @Override
    protected final void configureInbound(MessageSecurityMetadataSourceRegistry registry) {
        // Allow some basic access
        registry
                .simpTypeMatchers(CONNECT).authenticated()
                .simpTypeMatchers(UNSUBSCRIBE, DISCONNECT, HEARTBEAT).permitAll();

        // Add application specific checks
        customizeInbound(registry);

        // Deny everything else
        registry.anyMessage().denyAll();
    }

    protected abstract void customizeInbound(MessageSecurityMetadataSourceRegistry registry);

    protected void hasPermission(MessageSecurityMetadataSourceRegistry registry, String exchange, Enum<?> permission) {
        // TODO Allow more specific permission checks based on routing key
        String destination = buildDestination(exchange, "*");
        registry.simpSubscribeDestMatchers(destination)
                .access(String.format("hasPermission(null, T(%s).%s)", permission.getDeclaringClass().getName(), permission.name()));
    }

    protected String buildDestination(String exchange, String key) {
        return String.format("/exchange/%s/%s", exchange, key);
    }
}
