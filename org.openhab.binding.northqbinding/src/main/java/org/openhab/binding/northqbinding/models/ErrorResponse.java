package org.openhab.binding.northqbinding.models;

import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;

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
