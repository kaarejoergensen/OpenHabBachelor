package org.openhab.binding.northqbinding.exceptions;

@SuppressWarnings("serial")
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
