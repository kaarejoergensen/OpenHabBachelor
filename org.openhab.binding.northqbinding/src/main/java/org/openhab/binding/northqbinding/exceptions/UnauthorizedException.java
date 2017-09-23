package org.openhab.binding.northqbinding.exceptions;

@SuppressWarnings("serial")
public class UnauthorizedException extends APIException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(Throwable t) {
        super(t);
    }

    public UnauthorizedException(String message, Throwable t) {
        super(message, t);
    }
}
