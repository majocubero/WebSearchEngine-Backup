package ri.wse.support.exceptions;

public class InvalidJSONException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    public InvalidJSONException() {
    }

    /**
     * Creates a new exception with the specified message
     * @param message the message to display
     */
    public InvalidJSONException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the specified wrapped exception
     * @param cause the cause of the exception
     */
    public InvalidJSONException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new exception with the specified message and wrapped exception
     * @param message the message to display
     * @param cause the cause of the exception
     */
    public InvalidJSONException(String message, Throwable cause) {
        super(message, cause);
    }

}
