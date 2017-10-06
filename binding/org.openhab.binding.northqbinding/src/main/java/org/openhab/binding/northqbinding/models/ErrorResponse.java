/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.northqbinding.models;

import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;

/**
 *
 * @author Kaare Joergensen - Initial contribution
 */
public class ErrorResponse {
    public static final Type gsonType = new TypeToken<ErrorResponse>() {
    }.getType();
    private boolean success;
    private String errors;
    private int code;

    public boolean isSuccess() {
        return success;
    }

    public String getErrors() {
        return errors;
    }

    public int getCode() {
        return code;
    }
}
