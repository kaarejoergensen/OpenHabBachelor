package org.openhab.binding.northqbinding.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.northqbinding.exceptions.APIException;
import org.openhab.binding.northqbinding.exceptions.GatewayOfflineException;
import org.openhab.binding.northqbinding.exceptions.InvalidGatewaySerialException;
import org.openhab.binding.northqbinding.exceptions.InvalidRequestException;
import org.openhab.binding.northqbinding.exceptions.NoActiveGatewaysException;
import org.openhab.binding.northqbinding.exceptions.NoActiveHousesException;
import org.openhab.binding.northqbinding.exceptions.NoActiveRoomsException;
import org.openhab.binding.northqbinding.models.BinarySensor;
import org.openhab.binding.northqbinding.models.BinarySwitch;
import org.openhab.binding.northqbinding.models.ErrorResponse;
import org.openhab.binding.northqbinding.models.Gateway;
import org.openhab.binding.northqbinding.models.House;
import org.openhab.binding.northqbinding.models.NorthQThing;
import org.openhab.binding.northqbinding.models.Room;
import org.openhab.binding.northqbinding.models.Thermostat;
import org.openhab.binding.northqbinding.models.Token;
import org.openhab.binding.northqbinding.network.HttpClient.Result;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class QStickBridge {
    private static final String BASE_URL = "https://homemanager.tv";

    private HttpClient httpClient;
    private Token token;
    private Gson gson;
    private List<House> houses;
    private List<Gateway> gateways;

    public QStickBridge(String user, String pass) throws APIException, IOException {
        this.httpClient = new HttpClient();
        this.gson = new Gson();
        authenticate(user, pass);
        houses = getHouses();
        gateways = new ArrayList<>();
        for (House house : houses) {
            gateways.addAll(getGateways(house.getId()));
        }
    }

    private void authenticate(String user, String pass) throws APIException, IOException {
        String url = BASE_URL + "/token/new.json";
        String post = "username=" + user + "&password=" + pass;
        Result result = httpClient.post(url, post);
        handleErrors(result);
        token = gson.fromJson(result.getBody(), Token.class);
    }

    public List<House> getHouses() throws APIException, IOException {
        String url = BASE_URL + "/main/getCurrentUserHouses?token=" + token.getToken() + "&user=" + token.getUser();
        Result result = httpClient.get(url);
        try {
            handleErrors(result);
        } catch (NoActiveHousesException e) {
            return new ArrayList<>();
        }
        return gson.fromJson(result.getBody(), House.gsonType);
    }

    public List<Gateway> getGateways(int houseId) throws APIException, IOException {
        String url = BASE_URL + "/main/getHouseGateways?token=" + token.getToken() + "&user=" + token.getUser()
                + "&house_id=" + houseId;
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
        String url = BASE_URL + "/main/getRoomsStatus?token=" + token.getToken() + "&user=" + token.getUser()
                + "&gateway=" + gatewaySerial;
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
        String url = BASE_URL + "/main/getGatewayStatus?token=" + token.getToken() + "&user=" + token.getUser()
                + "&gateway=" + gatewaySerial;
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
        List<BinarySwitch> binarySwitchs = gson.fromJson(jsonObject.getAsJsonArray("BinarySwitches"),
                BinarySwitch.gsonType);
        JsonObject dongle = jsonObject.getAsJsonObject("dongle");
        String gatewaySerial = dongle.get("serial").isJsonNull() ? "" : dongle.get("serial").getAsString();
        for (BinarySwitch binarySwitch : binarySwitchs) {
            binarySwitch.setGateway(gatewaySerial);
        }
        return binarySwitchs;
    }

    public List<Thermostat> getAllThermostats(List<String> gatewayStatuses) {
        List<Thermostat> thermostats = new ArrayList<>();
        for (String gatewayStatus : gatewayStatuses) {
            thermostats.addAll(getThermostats(gatewayStatus));
        }
        return thermostats;
    }

    public List<Thermostat> getThermostats(String gatewayStatus) {
        JsonObject jsonObject = (JsonObject) new JsonParser().parse(gatewayStatus);
        List<Thermostat> thermostats = gson.fromJson(jsonObject.getAsJsonArray("Thermostats"), Thermostat.gsonType);
        JsonObject dongle = jsonObject.getAsJsonObject("dongle");
        String gatewaySerial = dongle.get("serial").isJsonNull() ? "" : dongle.get("serial").getAsString();
        for (Thermostat thermoStat : thermostats) {
            thermoStat.setGateway(gatewaySerial);
        }
        return thermostats;
    }

    public void changeSwitchState(BinarySwitch binarySwitch) throws IOException, APIException {
        boolean turnedOn = binarySwitch.isTurnedOn();
        String url = BASE_URL + "/main/setBinaryValue?token=" + token.getToken() + "&user=" + token.getUser();
        String post = "gateway=" + binarySwitch.getGateway() + "&node_id=" + binarySwitch.getNode_id() + "&pos="
                + (turnedOn ? 0 : 255);
        Result result = httpClient.post(url, post);
        handleErrors(result);
    }

    public void setRoomTemperature(Room room, double newTemperature) throws IOException, APIException {
        String url = BASE_URL + "/main/setRoomTemperature?token=" + token.getToken() + "&user=" + token.getUser();
        String post = "gateway=" + room.getGateway() + "&room_id=" + room.getId() + "&temperature=" + newTemperature;
        Result result = httpClient.post(url, post);
        handleErrors(result);
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
        List<BinarySensor> binarySensors = gson.fromJson(jsonObject.getAsJsonArray("BinarySensors"),
                BinarySensor.gsonType);
        JsonObject dongle = jsonObject.getAsJsonObject("dongle");
        String gatewaySerial = dongle.get("serial").isJsonNull() ? "" : dongle.get("serial").getAsString();
        for (BinarySensor binarySensor : binarySensors) {
            binarySensor.setGateway(gatewaySerial);
        }
        return binarySensors;
    }

    public void disArmSensor(BinarySensor binarySensor) throws IOException, APIException {
        String url = BASE_URL + "/main/disArmUserComponent?token=" + token.getToken() + "&user=" + token.getUser();
        String post = "gateway_id=" + binarySensor.getGateway() + "&node_id=" + binarySensor.getNode_id();
        Result result = httpClient.post(url, post);
        handleErrors(result);
    }

    public void armSensor(BinarySensor binarySensor) throws IOException, APIException {
        String url = BASE_URL + "/main/reArmUserComponent?token=" + token.getToken() + "&user=" + token.getUser();
        String post = "gateway_id=" + binarySensor.getGateway() + "&node_id=" + binarySensor.getNode_id();
        Result result = httpClient.post(url, post);
        handleErrors(result);
    }

    private void handleErrors(HttpClient.Result result) throws IOException, APIException {
        if (result.getResponseCode() != 200) {
            throw new IOException();
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
}