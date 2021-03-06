package models;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class BinarySwitch {
    public static final Type gsonType = new TypeToken<List<BinarySwitch>>() {}.getType();
    private int node_id;
    private String name;
    private int pos;
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
        return pos == 255;
    }

    public void setTurnedOn(boolean turnedOn) {
        this.pos = turnedOn ? 255 : 0;
    }

    public double getWattage() {
        return wattage;
    }

    public void setWattage(double wattage) {
        this.wattage = wattage;
    }
}
