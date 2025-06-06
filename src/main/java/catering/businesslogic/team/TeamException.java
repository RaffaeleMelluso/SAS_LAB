package catering.businesslogic.team;

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
