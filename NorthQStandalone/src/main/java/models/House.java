package models;

import org.json.JSONException;
import org.json.JSONObject;

public class House {
    private int id;
    private String name;
    private String type;
    private String country;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public static House parseJSON(JSONObject body) throws JSONException {
        House house = new House();

        house.setId(body.getInt("id"));
        house.setName(body.getString("name"));
        house.setType(body.getString("type"));
        house.setCountry(body.getString("country"));

        return house;
    }
}
