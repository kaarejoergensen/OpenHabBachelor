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
public class BinarySensor extends NorthQThing {
    public static final Type gsonType = new TypeToken<List<BinarySensor>>() {
    }.getType();
    private int battery;
    private int pos;
    private int armed;
    private List<Sensor> sensors;

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
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

    public boolean isMotionDetected() {
        return pos == 255;
    }

    @Override
    public String toString() {
        return "BinarySensor{" + "node_id=" + node_id + ", room=" + room + ", battery=" + battery + ", name='" + name
                + '\'' + ", armed=" + armed + ", sensorList="
                + sensors.stream().map(Sensor::toString).reduce("", String::concat) + '}';
    }

    public static class Sensor {
        public enum Type {
            TEMPERATURE,
            lUMINANCE,
            HUMIDITY;
        }

        private Type getType(int type) {
            switch (type) {
                case 1:
                    return Type.TEMPERATURE;
                case 3:
                    return Type.lUMINANCE;
                case 5:
                    return Type.HUMIDITY;
                default:
                    return null;
            }
        }

        private int scale;
        private int type;
        private double value;

        public int getScale() {
            return scale;
        }

        public void setScale(int scale) {
            this.scale = scale;
        }

        public Type getType() {
            return getType(type);
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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + scale;
            result = prime * result + type;
            long temp;
            temp = Double.doubleToLongBits(value);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Sensor other = (Sensor) obj;
            if (scale != other.scale) {
                return false;
            }
            if (type != other.type) {
                return false;
            }
            if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value)) {
                return false;
            }
            return true;
        }
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        return NorthQBindingBindingConstants.BINARY_SENSOR;
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
        BinarySensor other = (BinarySensor) obj;
        if (armed != other.armed) {
            return false;
        }
        if (pos != other.pos) {
            return false;
        }
        if (sensors == null) {
            if (other.sensors != null) {
                return false;
            }
        } else if (!sensors.equals(other.sensors)) {
            return false;
        }
        return true;
    }
}
