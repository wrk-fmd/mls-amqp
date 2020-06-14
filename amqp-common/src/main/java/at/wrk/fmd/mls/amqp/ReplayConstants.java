package at.wrk.fmd.mls.amqp;

/**
 * This class contains the names of replay related exchanges and headers
 */
public class ReplayConstants {

    public static final String REPLAY_TRIGGER_EXCHANGE = "replay.trigger";
    public static final String ROUTING_KEY_HEADER = "x-replay-routing-key";
}
