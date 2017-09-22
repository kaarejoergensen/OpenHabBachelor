package org.openhab.binding.northqbinding.exceptions;

@SuppressWarnings("serial")
public class NoActiveHousesException extends APIException {
    public NoActiveHousesException(String message) {
        super(message);
    }

    public NoActiveHousesException(Throwable t) {
        super(t);
    }

    public NoActiveHousesException(String message, Throwable t) {
        super(message, t);
    }
}
