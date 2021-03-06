/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.northq.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.openhab.binding.northq.exceptions.APIException;
import org.openhab.binding.northq.exceptions.GatewayOfflineException;
import org.openhab.binding.northq.exceptions.InvalidGatewaySerialException;
import org.openhab.binding.northq.exceptions.InvalidRequestException;
import org.openhab.binding.northq.exceptions.NoActiveGatewaysException;
import org.openhab.binding.northq.exceptions.NoActiveHousesException;
import org.openhab.binding.northq.exceptions.NoActiveRoomsException;
import org.openhab.binding.northq.exceptions.UnauthorizedException;
import org.openhab.binding.northq.models.BinarySensor;
import org.openhab.binding.northq.models.BinarySwitch;
import org.openhab.binding.northq.models.ErrorResponse;
import org.openhab.binding.northq.models.Gateway;
import org.openhab.binding.northq.models.House;
import org.openhab.binding.northq.models.NorthQThing;
import org.openhab.binding.northq.models.Room;
import org.openhab.binding.northq.models.Thermostat;
import org.openhab.binding.northq.models.Token;
import org.openhab.binding.northq.network.HttpClient.Result;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Handlse all communication with NorthQ Servers
 *
 * @author Kaare Joergensen - Initial contribution
 */
public class QStickBridge {
    private static final String BASE_URL = "https://homemanager.tv";
    private static final String AUTHENTICATE_URL = "/token/new.json";
    private static final String HOUSES_URL = "/main/getCurrentUserHouses?token=%s&user=%d";
    private static final String GATEWAY_URL = "/main/getHouseGateways?token=%s&user=%d&house_id=%d";
    private static final String ROOM_URL = "/main/getRoomsStatus?token=%s&user=%d&gateway=%s";
    private static final String GATEWAY_STATUS_URL = "/main/getGatewayStatus?token=%s&user=%d&gateway=%s";
    private static final String SWITCH_STATE_URL = "/main/setBinaryValue?token=%s&user=%d";
    private static final String SWITCH_STATE_POST = "gateway=%s&node_id=%d&pos=%d";
    private static final String SET_TEMPERATURE_URL = "/main/setRoomTemperature?token=%s&user=%d";
    private static final String SET_TEMPERATURE_POST = "gateway=%s&room_id=%d&temperature=%f";
    private static final String ARM_SENSOR_URL = "/main/reArmUserComponent?token=%s&user=%d";
    private static final String ARM_SENSOR_POST = "gateway_id=%s&node_id=%d";
    private static final String DISARM_SENSOR_URL = "/main/disArmUserComponent?token=%s&user=%d";
    private static final String DISARM_SENSOR_POST = ARM_SENSOR_POST;
    private static final String THERMOSTATS = "Thermostats";
    private static final String SERIAL = "serial";
    private static final String DONGLE = "dongle";
    private static final String BINARY_SWITCHES = "BinarySwitches";
    private static final String BINARY_SENSORS = "BinarySensors";

    private HttpClient httpClient;
    private Token token;
    private Gson gson;
    private List<House> houses;
    private List<Gateway> gateways;

    public QStickBridge() {
        this.httpClient = new HttpClient();
        this.gson = new GsonBuilder().registerTypeAdapter(Boolean.class, booleanAsIntAdapter)
                .registerTypeAdapter(boolean.class, booleanAsIntAdapter).create();
    }

    public QStickBridge(String user, String pass) throws APIException, IOException {
        this.httpClient = new HttpClient();
        this.gson = new GsonBuilder().registerTypeAdapter(Boolean.class, booleanAsIntAdapter)
                .registerTypeAdapter(boolean.class, booleanAsIntAdapter).create();
        authenticate(user, pass);
        updateHousesAndGateways();
    }

    public void authenticate(String user, String pass) throws APIException, IOException {
        String url = BASE_URL + AUTHENTICATE_URL;
        String post = "username=" + user + "&password=" + pass;
        Result result = httpClient.post(url, post);
        handleErrors(result);
        token = gson.fromJson(result.getBody(), Token.class);
    }

    public void updateHousesAndGateways() throws APIException, IOException {
        houses = getHouses();
        gateways = getAllGateways();
    }

    public List<House> getHouses() throws APIException, IOException {
        String url = BASE_URL + String.format(HOUSES_URL, token.getToken(), token.getUser());
        Result result = httpClient.get(url);
        try {
            handleErrors(result);
        } catch (NoActiveHousesException e) {
            return new ArrayList<>();
        }
        return gson.fromJson(result.getBody(), House.gsonType);
    }

    public List<Gateway> getAllGateways() throws APIException, IOException {
        List<Gateway> gateways = new ArrayList<>();
        for (House house : houses) {
            gateways.addAll(getGateways(house.getId()));
        }
        return gateways;
    }

    public List<Gateway> getGateways(int houseId) throws APIException, IOException {
        String url = BASE_URL + String.format(GATEWAY_URL, token.getToken(), token.getUser(), houseId);
        Result result = httpClient.get(url);
        try {
            handleErrors(result);
        } catch (NoActiveGatewaysException e) {
            return new ArrayList<>();
        }
        List<Gateway> gateways = gson.fromJson(result.getBody(), Gateway.gsonType);
        for (Gateway gateway : gateways) {
            gateway.setHouse(houseId);
        }
        return gateways;
    }

    public List<Room> getAllRooms() throws APIException, IOException {
        List<Room> rooms = new ArrayList<>();
        for (Gateway gateway : gateways) {
            rooms.addAll(getRooms(gateway.getSerial_nr()));
        }
        return rooms;
    }

    public List<Room> getRooms(String gatewaySerial) throws APIException, IOException {
        String url = BASE_URL + String.format(ROOM_URL, token.getToken(), token.getUser(), gatewaySerial);
        Result result = httpClient.get(url);
        try {
            handleErrors(result);
        } catch (NoActiveRoomsException e) {
            return new ArrayList<>();
        }
        List<Room> rooms = gson.fromJson(result.getBody(), Room.gsonType);
        for (Room room : rooms) {
            room.setGateway(gatewaySerial);
        }
        return rooms;
    }

    public List<String> getAllGatewayStatuses() throws APIException, IOException {
        List<String> gatewayStatuses = new ArrayList<>();
        for (Gateway gateway : gateways) {
            gatewayStatuses.add(getGatewayStatus(gateway.getSerial_nr()));
        }
        return gatewayStatuses;
    }

    public String getGatewayStatus(String gatewaySerial) throws APIException, IOException {
        String url = BASE_URL + String.format(GATEWAY_STATUS_URL, token.getToken(), token.getUser(), gatewaySerial);
        Result result = httpClient.get(url);
        handleErrors(result);
        return result.getBody();
    }

    public List<NorthQThing> getAllThings(List<String> gatewayStatuses) {
        List<NorthQThing> things = new ArrayList<>();

        things.addAll(getAllBinarySensors(gatewayStatuses));
        things.addAll(getAllSwitches(gatewayStatuses));
        things.addAll(getAllThermostats(gatewayStatuses));

        return things;
    }

    public List<BinarySwitch> getAllSwitches(List<String> gatewayStatuses) {
        List<BinarySwitch> binarySwitchs = new ArrayList<>();
        for (String gatewayStatus : gatewayStatuses) {
            binarySwitchs.addAll(getSwitches(gatewayStatus));
        }
        return binarySwitchs;
    }

    public List<BinarySwitch> getSwitches(String gatewayStatus) {
        JsonObject jsonObject = (JsonObject) new JsonParser().parse(gatewayStatus);
        List<BinarySwitch> binarySwitchs = gson.fromJson(jsonObject.getAsJsonArray(BINARY_SWITCHES),
                BinarySwitch.gsonType);
        JsonObject dongle = jsonObject.getAsJsonObject(DONGLE);
        if (binarySwitchs != null && dongle != null) {
            String gatewaySerial = dongle.get(SERIAL).isJsonNull() ? "" : dongle.get(SERIAL).getAsString();
            for (BinarySwitch binarySwitch : binarySwitchs) {
                binarySwitch.setGateway(gatewaySerial);
            }
            return binarySwitchs;
        } else {
            return Collections.emptyList();
        }
    }

    public List<Thermostat> getAllThermostats(List<String> gatewayStatuses) {
        List<Thermostat> thermostats = new ArrayList<>();
        gatewayStatuses.stream().forEach(gs -> thermostats.addAll(getThermostats(gs)));
        return thermostats;
    }

    public List<Thermostat> getThermostats(String gatewayStatus) {
        JsonObject jsonObject = (JsonObject) new JsonParser().parse(gatewayStatus);
        List<Thermostat> thermostats = gson.fromJson(jsonObject.getAsJsonArray(THERMOSTATS), Thermostat.gsonType);
        JsonObject dongle = jsonObject.getAsJsonObject(DONGLE);
        if (thermostats != null && dongle != null) {
            String gatewaySerial = dongle.get(SERIAL).isJsonNull() ? "" : dongle.get(SERIAL).getAsString();
            thermostats.stream().forEach(th -> th.setGateway(gatewaySerial));
            // Sort the list with the most recently read thermostats first
            return thermostats.stream().sorted(Comparator.comparing(Thermostat::getRead).reversed())
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public Boolean changeSwitchState(BinarySwitch binarySwitch) throws IOException, APIException {
        boolean turnedOn = binarySwitch.isTurnedOn();
        String url = BASE_URL + String.format(SWITCH_STATE_URL, token.getToken(), token.getUser());
        String post = String.format(SWITCH_STATE_POST, binarySwitch.getGateway(), binarySwitch.getNode_id(),
                (turnedOn ? 0 : 255));
        Result result = httpClient.post(url, post);
        handleErrors(result);
        ErrorResponse errorResponse = gson.fromJson(result.getBody(), ErrorResponse.gsonType);
        return errorResponse.isSuccess();
    }

    public Boolean setRoomTemperature(int roomId, String gatewaySerial, double newTemperature)
            throws IOException, APIException {
        String url = BASE_URL + String.format(SET_TEMPERATURE_URL, token.getToken(), token.getUser());
        String post = String.format(SET_TEMPERATURE_POST, gatewaySerial, roomId, newTemperature);
        Result result = httpClient.post(url, post);
        handleErrors(result);
        ErrorResponse errorResponse = gson.fromJson(result.getBody(), ErrorResponse.gsonType);
        return errorResponse.isSuccess();
    }

    public List<BinarySensor> getAllBinarySensors(List<String> gatewayStatuses) {
        List<BinarySensor> binarySensors = new ArrayList<>();
        for (String gatewayStatus : gatewayStatuses) {
            binarySensors.addAll(getBinarySensors(gatewayStatus));
        }
        return binarySensors;
    }

    public List<BinarySensor> getBinarySensors(String gatewayStatus) {
        JsonObject jsonObject = (JsonObject) new JsonParser().parse(gatewayStatus);
        List<BinarySensor> binarySensors = gson.fromJson(jsonObject.getAsJsonArray(BINARY_SENSORS),
                BinarySensor.gsonType);
        JsonObject dongle = jsonObject.getAsJsonObject(DONGLE);
        if (binarySensors != null && dongle != null) {
            String gatewaySerial = dongle.get(SERIAL).isJsonNull() ? "" : dongle.get(SERIAL).getAsString();
            for (BinarySensor binarySensor : binarySensors) {
                binarySensor.setGateway(gatewaySerial);
            }
            return binarySensors;
        } else {
            return Collections.emptyList();
        }
    }

    public Boolean disArmSensor(BinarySensor binarySensor) throws IOException, APIException {
        String url = BASE_URL + String.format(DISARM_SENSOR_URL, token.getToken(), token.getUser());
        String post = String.format(DISARM_SENSOR_POST, binarySensor.getGateway(), binarySensor.getNode_id());
        Result result = httpClient.post(url, post);
        handleErrors(result);
        ErrorResponse errorResponse = gson.fromJson(result.getBody(), ErrorResponse.gsonType);
        return errorResponse.isSuccess();
    }

    public Boolean armSensor(BinarySensor binarySensor) throws IOException, APIException {
        String url = BASE_URL + String.format(ARM_SENSOR_URL, token.getToken(), token.getUser());
        String post = String.format(ARM_SENSOR_POST, binarySensor.getGateway(), binarySensor.getNode_id());
        Result result = httpClient.post(url, post);
        handleErrors(result);
        ErrorResponse errorResponse = gson.fromJson(result.getBody(), ErrorResponse.gsonType);
        return errorResponse.isSuccess();
    }

    private void handleErrors(HttpClient.Result result) throws IOException, APIException {
        if (result == null) {
            throw new APIException("Result null");
        }
        if (result.getResponseCode() != 200) {
            if (result.getResponseCode() == 401 || result.getResponseCode() == 403) {
                throw new UnauthorizedException(result.getBody());
            } else {
                throw new IOException();
            }
        } else {
            try {
                ErrorResponse errorResponse = gson.fromJson(result.getBody(), ErrorResponse.gsonType);
                if (errorResponse == null || errorResponse.isSuccess() || errorResponse.getErrors() == null) {
                    return;
                }

                switch (errorResponse.getCode()) {
                    case 999:
                        throw new InvalidRequestException(errorResponse.getErrors());
                    case 1000:
                        throw new NoActiveHousesException(errorResponse.getErrors());
                    case 1001:
                        throw new NoActiveGatewaysException(errorResponse.getErrors());
                    case 1002:
                        throw new InvalidGatewaySerialException(errorResponse.getErrors());
                    case 1003:
                        throw new GatewayOfflineException(errorResponse.getErrors());
                    case 1004:
                        throw new NoActiveRoomsException(errorResponse.getErrors());
                    default:
                        throw new APIException(errorResponse.getErrors());
                }
            } catch (JsonParseException e) {
                // JSON body does not contain an error
            }

        }
    }

    // https://stackoverflow.com/questions/11399079/convert-ints-to-booleans
    private static final TypeAdapter<Boolean> booleanAsIntAdapter = new TypeAdapter<Boolean>() {
        @Override
        public void write(JsonWriter out, Boolean value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value);
            }
        }

        @Override
        public Boolean read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            switch (peek) {
                case BOOLEAN:
                    return in.nextBoolean();
                case NULL:
                    in.nextNull();
                    return null;
                case NUMBER:
                    return in.nextInt() != 0;
                case STRING:
                    return in.nextString().equalsIgnoreCase("1");
                default:
                    throw new IllegalStateException("Expected BOOLEAN or NUMBER but was " + peek);
            }
        }
    };
}
