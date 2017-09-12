package org.openhab.binding.northqbinding.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Gateway {
    private int id;
    private String serial;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public static Gateway parseJSON(JSONObject body) throws JSONException {
        Gateway gateway = new Gateway();

        gateway.setId(body.getInt("id"));
        gateway.setSerial(body.getString("serial_nr"));

        return gateway;
    }
}
