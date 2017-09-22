package org.openhab.binding.northqbinding.exceptions;

@SuppressWarnings("serial")
public class GatewayOfflineException extends APIException {
    public GatewayOfflineException(String message) {
        super(message);
    }

    public GatewayOfflineException(Throwable t) {
        super(t);
    }

    public GatewayOfflineException(String message, Throwable t) {
        super(message, t);
    }
}
