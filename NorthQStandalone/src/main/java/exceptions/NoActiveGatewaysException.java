package exceptions;

public class NoActiveGatewaysException extends APIException {
    public NoActiveGatewaysException(String message) {
        super(message);
    }

    public NoActiveGatewaysException(Throwable t) {
        super(t);
    }

    public NoActiveGatewaysException(String message, Throwable t) {
        super(message, t);
    }
}
