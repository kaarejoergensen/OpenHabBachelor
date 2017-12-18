/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.northq.osgi.helper;

import java.io.IOException;
import java.util.Collections;

import org.openhab.binding.northq.models.BinarySensor;
import org.openhab.binding.northq.models.BinarySwitch;
import org.openhab.binding.northq.models.Gateway;
import org.openhab.binding.northq.models.House;
import org.openhab.binding.northq.models.NorthQThing;
import org.openhab.binding.northq.models.Room;
import org.openhab.binding.northq.models.Thermostat;
import org.openhab.binding.northq.models.Token;
import org.openhab.binding.northq.network.HttpClient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Class for mocking HttpClient in tests.
 *
 * @author Kaare Joergensen - Initial contribution
 */
public class MockedHttpClient extends HttpClient {
    protected Gson gson;

    public MockedHttpClient() {
        this.gson = new Gson();
    }

    @Override
    public Result post(String address, String body) throws IOException {
        if (address.endsWith("/token/new.json")) {
            return new Result(gson.toJson(new Token("testToken", 1), Token.class), 200);
        } else {
            return null;
        }
    }

    @Override
    public Result get(String address) throws IOException {
        if (address.contains("getCurrentUserHouses")) {
            return new Result(gson.toJson(Collections.singletonList(new House(1, "Test house", "Apartment", "Denmark")),
                    House.gsonType), 200);
        } else if (address.contains("getHouseGateways")) {
            return new Result(gson.toJson(Collections.singletonList(new Gateway(1, "0000000001", 1)), Gateway.gsonType),
                    200);
        } else if (address.contains("getRoomsStatus")) {
            return new Result(
                    gson.toJson(Collections.singletonList(new Room(1, "Test room", 0, "0000000001")), Room.gsonType),
                    200);
        } else if (address.contains("getGatewayStatus")) {
            return new Result("{ }", 200);
        } else {
            return null;
        }
    }

    protected String createGatewayStatus(NorthQThing thing) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = new JsonObject();

        if (thing instanceof BinarySensor) {
            jsonObject.add("BinarySensors",
                    jsonParser.parse(gson.toJson(Collections.singletonList(thing), BinarySensor.gsonType)));
        } else if (thing instanceof BinarySwitch) {
            jsonObject.add("BinarySwitches",
                    jsonParser.parse(gson.toJson(Collections.singletonList(thing), BinarySwitch.gsonType)));
        } else if (thing instanceof Thermostat) {
            jsonObject.add("Thermostats",
                    jsonParser.parse(gson.toJson(Collections.singletonList(thing), Thermostat.gsonType)));
        }
        JsonObject dongle = new JsonObject();
        dongle.addProperty("serial", "0000000001");
        jsonObject.add("dongle", dongle);

        return jsonObject.toString();
    }
}
