package at.wrk.fmd.mls.event;

/**
 * This exception can be thrown by event handlers if an event was considered invalid
 */
public class InvalidEventException extends RuntimeException {

    public InvalidEventException() {
    }

    public InvalidEventException(String message) {
        super(message);
    }

    public InvalidEventException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidEventException(Throwable cause) {
        super(cause);
    }
}
