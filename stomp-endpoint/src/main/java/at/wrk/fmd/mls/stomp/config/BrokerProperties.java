package at.wrk.fmd.mls.stomp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application.broker")
public class BrokerProperties {

    /**
     * Host of the broker handling the STOMP subscriptions
     */
    private String host;

    /**
     * Username for authenticating with the STOMP broker
     */
    private String username;

    /**
     * Password for authenticating with the STOMP broker
     */
    private String password;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
