package at.wrk.fmd.mls.stomp.config;

import at.wrk.fmd.mls.stomp.interceptor.StompTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * This class is used to add an interceptor for JWT tokens to the WebSocket
 * It needs to run before the regular Spring Security interceptors
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
class StompTokenConfig implements WebSocketMessageBrokerConfigurer {

    private final StompTokenInterceptor tokenInterceptor;

    @Autowired
    public StompTokenConfig(StompTokenInterceptor tokenInterceptor) {
        this.tokenInterceptor = tokenInterceptor;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(tokenInterceptor);
    }
}
