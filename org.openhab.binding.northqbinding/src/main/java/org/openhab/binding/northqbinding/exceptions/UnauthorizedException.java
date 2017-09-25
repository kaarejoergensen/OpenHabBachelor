/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.northqbinding.exceptions;

/**
 *
 * @author Kaare Joergensen - Initial contribution
 */
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
