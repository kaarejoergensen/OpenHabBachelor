package exceptions;

public class APIException extends Exception {

    public APIException(String message) {
        super(message);
    }

    public APIException(Throwable t) {
        super(t);
    }

    public APIException(String message, Throwable t) {
        super(message, t);
    }
}
