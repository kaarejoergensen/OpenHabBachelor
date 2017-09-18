package org.openhab.binding.northqbinding.models;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;

public class BinarySwitch extends NorthQThing {
    public static final Type gsonType = new TypeToken<List<BinarySwitch>>() {
    }.getType();
    private int pos;
    private double wattage;

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
