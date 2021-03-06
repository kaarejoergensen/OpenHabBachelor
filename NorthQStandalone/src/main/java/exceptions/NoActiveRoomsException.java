package exceptions;

public class NoActiveRoomsException extends APIException {
    public NoActiveRoomsException(String message) {
        super(message);
    }

    public NoActiveRoomsException(Throwable t) {
        super(t);
    }

    public NoActiveRoomsException(String message, Throwable t) {
        super(message, t);
    }
}
