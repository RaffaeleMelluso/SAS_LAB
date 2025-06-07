package catering.businesslogic.team;

/**
 * Exception thrown for errors related to team operations.
 */
public class TeamException extends Exception {
    public TeamException(String message) {
        super(message);
    }

    public TeamException(String message, Throwable cause) {
        super(message, cause);
    }

    public TeamException(Throwable cause) {
        super(cause);
    }

    public TeamException() {
        super();
    }
}