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

    public ErrorResponse(boolean success, String errors) {
        this.success = success;
        this.errors = errors;
    }

    public ErrorResponse(boolean success, String errors, int code) {
        this.success = success;
        this.errors = errors;
        this.code = code;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
