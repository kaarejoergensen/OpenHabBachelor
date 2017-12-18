/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.northq.models;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;

/**
 *
 * @author Kaare Joergensen - Initial contribution
 */
public class Gateway {
    public static final Type gsonType = new TypeToken<List<Gateway>>() {
    }.getType();
    private int id;
    private String serial_nr;
    private int house;

    public Gateway(int id, String serial_nr, int house) {
        this.id = id;
        this.serial_nr = serial_nr;
        this.house = house;
    }

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
