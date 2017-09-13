package models;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class ErrorResponse {
    public static final Type gsonType = new TypeToken<ErrorResponse>() {}.getType();
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
