/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.northqbinding.models;

import java.lang.reflect.Type;
import java.util.List;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.northqbinding.NorthQBindingBindingConstants;

import com.google.gson.reflect.TypeToken;

/**
 *
 * @author Kaare Joergensen - Initial contribution
 */
public class Thermostat extends NorthQThing {
    public static Type gsonType = new TypeToken<List<Thermostat>>() {
    }.getType();

    private int battery;
    private int temperature;

    public Thermostat(int battery, int temperature) {
        this.battery = battery;
        this.temperature = temperature;
    }

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
    public String getUniqueId() {
        return String.format("%s%d%s", this.gateway, this.room, this.getThingTypeUID().getId());
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        return NorthQBindingBindingConstants.THERMOSTAT;
    }

    @Override
    public boolean isEqual(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Thermostat other = (Thermostat) obj;
        if (temperature != other.temperature) {
            return false;
        }
        return true;
    }

}
