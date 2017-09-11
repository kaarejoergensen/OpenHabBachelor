package models;

import org.json.JSONException;
import org.json.JSONObject;

public class Token {
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

    public static Token parseJSON(JSONObject body) throws JSONException {
        Token token = new Token();

        token.setToken(body.getString("token"));
        token.setUser(body.getInt("user"));

        return token;
    }
}
