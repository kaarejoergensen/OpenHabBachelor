package models;

import org.json.JSONException;
import org.json.JSONObject;

public class Room {
    private int id;
    private String name;
    private double temperature;

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

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public static Room parseJSON(JSONObject body) throws JSONException {
        Room room = new Room();

        room.setId(body.getInt("id"));
        room.setName(body.getString("name"));
        room.setTemperature(body.getDouble("temperature"));

        return room;
    }
}
