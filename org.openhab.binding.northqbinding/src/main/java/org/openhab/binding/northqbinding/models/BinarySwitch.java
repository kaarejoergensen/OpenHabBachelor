package org.openhab.binding.northqbinding.models;

import org.json.JSONException;
import org.json.JSONObject;

public class BinarySwitch {
    private int node_id;
    private String name;
    private boolean turnedOn;
    private double wattage;

    public int getNode_id() {
        return node_id;
    }

    public void setNode_id(int node_id) {
        this.node_id = node_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isTurnedOn() {
        return turnedOn;
    }

    public void setTurnedOn(boolean turnedOn) {
        this.turnedOn = turnedOn;
    }

    public double getWattage() {
        return wattage;
    }

    public void setWattage(double wattage) {
        this.wattage = wattage;
    }

    public static BinarySwitch parseJSON(JSONObject body) throws JSONException {
        BinarySwitch binarySwitch = new BinarySwitch();

        binarySwitch.setNode_id(body.getInt("node_id"));
        binarySwitch.setName(body.getString("name"));
        binarySwitch.setTurnedOn(body.getInt("pos") == 255);
        binarySwitch.setWattage(body.getDouble("wattage"));

        return binarySwitch;
    }
}
