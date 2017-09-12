package org.openhab.binding.northqbinding.models;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BinarySensor {
    private int node_id;
    private int room;
    private int battery;
    private String name;
    private boolean armed;
    private List<Sensor> sensorList;

    public int getNode_id() {
        return node_id;
    }

    public void setNode_id(int node_id) {
        this.node_id = node_id;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isArmed() {
        return armed;
    }

    public void setArmed(boolean armed) {
        this.armed = armed;
    }

    public List<Sensor> getSensorList() {
        return sensorList;
    }

    public void setSensorList(List<Sensor> sensorList) {
        this.sensorList = sensorList;
    }

    @Override
    public String toString() {
        return "BinarySensor{" + "node_id=" + node_id + ", room=" + room + ", battery=" + battery + ", name='" + name
                + '\'' + ", armed=" + armed + ", sensorList="
                + sensorList.stream().map(Sensor::toString).reduce("", String::concat) + '}';
    }

    public static BinarySensor parseJSON(JSONObject body) throws JSONException {
        BinarySensor binarySensor = new BinarySensor();

        binarySensor.setNode_id(body.getInt("node_id"));
        binarySensor.setRoom(body.getInt("room"));
        binarySensor.setBattery(body.getInt("battery"));
        binarySensor.setName(body.getString("name"));
        binarySensor.setArmed(body.getInt("armed") == 1);
        JSONArray jsonArray = body.getJSONArray("sensors");
        List<Sensor> sensors = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            sensors.add(Sensor.parseJSON(jsonArray.getJSONObject(i)));
        }
        binarySensor.setSensorList(sensors);

        return binarySensor;
    }

    public static class Sensor {
        private int scale;
        private int type;
        private double value;

        public int getScale() {
            return scale;
        }

        public void setScale(int scale) {
            this.scale = scale;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Sensor{" + "scale=" + scale + ", type=" + type + ", value=" + value + '}';
        }

        public static Sensor parseJSON(JSONObject body) throws JSONException {
            Sensor sensor = new Sensor();

            sensor.setScale(body.getInt("scale"));
            sensor.setType(body.getInt("type"));
            sensor.setValue(body.getDouble("value"));

            return sensor;
        }
    }
}
