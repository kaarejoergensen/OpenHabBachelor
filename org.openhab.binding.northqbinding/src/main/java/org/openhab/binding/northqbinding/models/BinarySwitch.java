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

    @Override
    public ThingTypeUID getThingTypeUID() {
        return NorthQBindingBindingConstants.BINARY_SWITCH;
    }
}
