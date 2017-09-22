package org.openhab.binding.northqbinding.models;

import java.lang.reflect.Type;

import com.google.common.reflect.TypeToken;

public class Token {
    @SuppressWarnings("serial")
    public static final Type gsonType = new TypeToken<Token>() {
    }.getType();

    private String token;
    private Integer user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getUser() {
        return user;
    }

    public void setUser(Integer user) {
        this.user = user;
    }
}
