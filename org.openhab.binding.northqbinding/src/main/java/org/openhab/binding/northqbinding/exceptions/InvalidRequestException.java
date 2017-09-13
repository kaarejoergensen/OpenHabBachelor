package org.openhab.binding.northqbinding.exceptions;

public class InvalidRequestException extends APIException {
    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(Throwable t) {
        super(t);
    }

    public InvalidRequestException(String message, Throwable t) {
        super(message, t);
    }
}
