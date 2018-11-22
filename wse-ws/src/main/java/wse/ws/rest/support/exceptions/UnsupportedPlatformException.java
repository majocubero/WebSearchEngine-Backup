package wse.ws.rest.support.exceptions;

/**
 * A runtime exception that is thrown when a platform not included in {@link OJO} is requested
 * by a web service
 */
public class UnsupportedPlatformException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    public UnsupportedPlatformException() {
    }

    /**
     * Creates a new exception with the specified message
     * @param message the message to display
     */
    public UnsupportedPlatformException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the specified wrapped exception
     * @param cause the cause of the exception
     */
    public UnsupportedPlatformException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new exception with the specified message and wrapped exception
     * @param message the message to display
     * @param cause the cause of the exception
     */
    public UnsupportedPlatformException(String message, Throwable cause) {
        super(message, cause);
    }

}
