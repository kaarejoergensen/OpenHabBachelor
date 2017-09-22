package org.openhab.binding.northqbinding.exceptions;

@SuppressWarnings("serial")
public class InvalidGatewaySerialException extends APIException {
    public InvalidGatewaySerialException(String message) {
        super(message);
    }

    public InvalidGatewaySerialException(Throwable t) {
        super(t);
    }

    public InvalidGatewaySerialException(String message, Throwable t) {
        super(message, t);
    }
}
