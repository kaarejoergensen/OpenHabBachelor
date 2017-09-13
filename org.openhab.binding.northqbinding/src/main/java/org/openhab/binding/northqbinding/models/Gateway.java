package org.openhab.binding.northqbinding.models;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;

public class Gateway {
    public static final Type gsonType = new TypeToken<List<Gateway>>() {
    }.getType();
    private int id;
    private String serial_nr;
    private int house;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSerial_nr() {
        return serial_nr;
    }

    public void setSerial_nr(String serial_nr) {
        this.serial_nr = serial_nr;
    }

    public int getHouse() {
        return house;
    }

    public void setHouse(int house) {
        this.house = house;
    }

}
