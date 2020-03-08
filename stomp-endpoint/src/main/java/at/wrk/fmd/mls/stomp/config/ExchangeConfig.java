package at.wrk.fmd.mls.stomp.config;

import at.wrk.fmd.mls.replay.ReplayConstants;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ExchangeConfig {

    @Bean
    public Exchange exchangeReplayTrigger() {
        return new DirectExchange(ReplayConstants.REPLAY_TRIGGER_EXCHANGE);
    }
}
