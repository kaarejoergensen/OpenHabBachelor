/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.northq.exceptions;

/**
 *
 * @author Kaare Joergensen - Initial contribution
 */
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
