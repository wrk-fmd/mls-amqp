package at.wrk.fmd.mls.replay.dto;

/**
 * This message indicates that the entity with the given id has been deleted
 */
public class DeletionDto {

    @SuppressWarnings("FieldCanBeLocal")
    private final boolean deleted = true;
    private final long id;

    public DeletionDto(long id) {
        this.id = id;
    }

    /**
     * Indicates that an entity has been deleted
     *
     * @return Always true
     */
    public boolean isDeleted() {
        // This needs to be a field, otherwise JSON serialization won't pick it up
        return deleted;
    }

    /**
     * @return The id of the deleted entity
     */
    public long getId() {
        return id;
    }
}
