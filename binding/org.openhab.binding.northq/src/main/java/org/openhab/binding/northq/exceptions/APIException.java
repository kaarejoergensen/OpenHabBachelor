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
 * Is thrown if the api returns an error
 *
 * @author Kaare Joergensen - Initial contribution
 */
@SuppressWarnings("serial")
public class APIException extends Exception {

    public APIException(String message) {
        super(message);
    }

    public APIException(Throwable t) {
        super(t);
    }

    public APIException(String message, Throwable t) {
        super(message, t);
    }
}
