package org.openhab.binding.northqbinding.models;

import java.lang.reflect.Type;
import java.util.List;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.northqbinding.NorthQBindingBindingConstants;

import com.google.gson.reflect.TypeToken;

public class Thermostat extends NorthQThing {
    public static Type gsonType = new TypeToken<List<Thermostat>>() {
    }.getType();

    private int battery;
    private int temperature;

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        return NorthQBindingBindingConstants.THERMOSTAT;
    }
}
