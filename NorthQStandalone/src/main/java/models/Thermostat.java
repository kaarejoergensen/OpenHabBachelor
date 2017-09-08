package models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Thermostat {
    private int node_id;
    private int temperature;
    private int battery;
    private Date read;
    private int room;

    public int getNode_id() {
        return node_id;
    }

    public void setNode_id(int node_id) {
        this.node_id = node_id;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public Date getRead() {
        return read;
    }

    public void setRead(Date read) {
        this.read = read;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public static Thermostat parseJSON(JSONObject body) throws JSONException {
        Thermostat thermostat = new Thermostat();

        thermostat.setNode_id(body.getInt("node_id"));
        thermostat.setTemperature(body.getInt("temperature"));
        thermostat.setBattery(body.getInt("battery"));
        thermostat.setRead(new Date(body.getLong("read")*1000));
        thermostat.setRoom(body.getInt("room"));

        return thermostat;
    }
}
