package at.wrk.fmd.mls.replay.dto;

/**
 * This message indicates the start of a replay sequence
 * Subscribers should empty their local cache upon receiving this message as it might be stale
 */
public class ReplayStartDto {

    @SuppressWarnings("FieldCanBeLocal")
    private final boolean replay = true;
    private final String key;

    public ReplayStartDto(String key) {
        this.key = key;
    }

    /**
     * Indicates that the replay has been started
     *
     * @return Always true
     */
    public boolean isReplay() {
        // This needs to be a field, otherwise JSON serialization won't pick it up
        return replay;
    }

    /**
     * @return The routing key for which the data is being replayed
     */
    public String getKey() {
        return key;
    }
}
