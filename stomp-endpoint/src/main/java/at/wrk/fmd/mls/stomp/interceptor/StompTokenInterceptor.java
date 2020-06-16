package at.wrk.fmd.mls.stomp.interceptor;

import at.wrk.fmd.mls.auth.filter.JwtAuthenticationParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Component
public class StompTokenInterceptor implements ChannelInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final JwtAuthenticationParser authenticationParser;

    @Autowired
    public StompTokenInterceptor(JwtAuthenticationParser authenticationParser) {
        this.authenticationParser = authenticationParser;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        String token = accessor.getFirstNativeHeader("token");
        if (token == null) {
            return message;
        }
        LOG.debug("Received token {}", token);

        Authentication authentication = authenticationParser.getAuthentication(token, null);
        if (authentication != null) {
            LOG.info("User {} connected through STOMP", authentication.getPrincipal());
        }

        accessor.setUser(authentication);
        return message;
    }
}
