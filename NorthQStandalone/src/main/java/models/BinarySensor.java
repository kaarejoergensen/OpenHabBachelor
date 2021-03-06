package models;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class BinarySensor {
    public static final Type gsonType = new TypeToken<List<BinarySensor>>() {}.getType();
    private int node_id;
    private int room;
    private int battery;
    private String name;
    private int armed;
    private List<Sensor> sensors;

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
        return armed == 1;
    }

    public void setArmed(boolean armed) {
        this.armed = armed ? 1 : 0;
    }

    public List<Sensor> getSensorList() {
        return sensors;
    }

    public void setSensorList(List<Sensor> sensorList) {
        this.sensors = sensorList;
    }

    @Override
    public String toString() {
        return "BinarySensor{" +
                "node_id=" + node_id +
                ", room=" + room +
                ", battery=" + battery +
                ", name='" + name + '\'' +
                ", armed=" + armed +
                ", sensorList=" + sensors.stream().map(Sensor::toString).reduce("", String::concat) +
                '}';
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
            return "Sensor{" +
                    "scale=" + scale +
                    ", type=" + type +
                    ", value=" + value +
                    '}';
        }
    }
}
