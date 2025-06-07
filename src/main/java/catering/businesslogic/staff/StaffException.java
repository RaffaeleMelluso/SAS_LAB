package catering.businesslogic.staff;

public class StaffException extends Exception {

    public StaffException() {
        super();
    }

    public StaffException(String message) {
        super(message);
    }

    public StaffException(String message, Throwable cause) {
        super(message, cause);
    }

    public StaffException(Throwable cause) {
        super(cause);
    }
}
