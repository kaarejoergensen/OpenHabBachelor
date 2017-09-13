package network;

public class NetworkErrorException extends Exception {

    public NetworkErrorException(String message) {
        super(message);
    }

    public NetworkErrorException(Throwable t) {
        super(t);
    }

    public NetworkErrorException(String message, Throwable t) {
        super(message, t);
    }
}
