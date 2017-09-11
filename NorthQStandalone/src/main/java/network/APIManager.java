package network;

import models.*;
import okhttp3.OkHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class APIManager {
    private static final String BASE_URL = "https://homemanager.tv";
    private HttpHelper httpHelper;
    private Token token;

    public APIManager(OkHttpClient okHttpClient) {
        this.httpHelper = new HttpHelper(okHttpClient);
    }

    public void authenticate(String user, String pass) throws NetworkErrorException, JSONException {
        String url = BASE_URL + "/token/new.json";
        String post = "username=" + user + "&password=" + pass;
        String response = httpHelper.sendPost(url, post);
        JSONObject body = new JSONObject(response);
        token = Token.parseJSON(body);
    }

    public List<House> getHouses() throws NetworkErrorException, JSONException {
        String url = BASE_URL + "/main/getCurrentUserHouses?token=" + token.getToken() + "&user=" + token.getUser();
        String body = httpHelper.sendGet(url);
        List<House> houses = new ArrayList<>();
        JSONArray array = new JSONArray(body);
        for (int i = 0; i < array.length(); i++) {
            houses.add(House.parseJSON(array.getJSONObject(i)));
        }
        return houses;
    }

    public List<Gateway> getGateways(int houseId) throws NetworkErrorException, JSONException {
        String url = BASE_URL + "/main/getHouseGateways?token=" + token.getToken() + "&user=" + token.getUser() + "&house_id=" + houseId;
        List<Gateway> gateways = new ArrayList<>();
        String body = httpHelper.sendGet(url);
        JSONArray array = new JSONArray(body);
        for (int i = 0; i< array.length(); i++) {
            gateways.add(Gateway.parseJSON(array.getJSONObject(i)));
        }
        return gateways;
    }

    public List<Room> getRooms(String gatewaySerial) throws NetworkErrorException, JSONException {
        String url = BASE_URL + "/main/getRoomsStatus?token=" + token.getToken() + "&user=" + token.getUser() + "&gateway=" + gatewaySerial;
        List<Room> rooms = new ArrayList<>();
        String body = httpHelper.sendGet(url);
        JSONArray array = new JSONArray(body);
        for (int i = 0; i < array.length(); i++) {
            rooms.add(Room.parseJSON(array.getJSONObject(i)));
        }
        return rooms;
    }

    public String getGatewayStatus(String gatewaySerial) throws NetworkErrorException {
        String url = BASE_URL + "/main/getGatewayStatus?token=" + token.getToken() + "&user=" + token.getUser() + "&gateway=" + gatewaySerial;
        return httpHelper.sendGet(url);
    }

    public List<BinarySwitch> getSwitches(String gatewaySerial) throws NetworkErrorException, JSONException {
        String gatewayStatus = getGatewayStatus(gatewaySerial);
        List<BinarySwitch> binarySwitches = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(gatewayStatus);
        JSONArray array = jsonObject.getJSONArray("BinarySwitches");
        for (int i = 0; i < array.length(); i++) {
            binarySwitches.add(BinarySwitch.parseJSON(array.getJSONObject(i)));
        }
        return binarySwitches;
    }

    public void changeSwitchState(String gatewaySerial, BinarySwitch binarySwitch) throws NetworkErrorException {
        boolean turnedOn = binarySwitch.isTurnedOn();
        String url = BASE_URL + "/main/setBinaryValue?token=" + token.getToken() + "&user=" + token.getUser();
        String post = "gateway=" + gatewaySerial + "&node_id=" + binarySwitch.getNode_id() + "&pos=" + (turnedOn ? 0 : 255);
        httpHelper.sendPost(url, post);
    }

    public void setRoomTemperature(String gatewaySerial, Room room, double newTemperature) throws NetworkErrorException {
        String url = BASE_URL + "/main/setRoomTemperature?token=" + token.getToken() + "&user=" + token.getUser();
        String post = "gateway=" + gatewaySerial + "&room_id=" + room.getId() + "&temperature=" + newTemperature;
        httpHelper.sendPost(url, post);
    }

    public List<BinarySensor> getBinarySensors(String gatewaySerial) throws NetworkErrorException, JSONException {
        String gatewayStatus = getGatewayStatus(gatewaySerial);
        List<BinarySensor> binarySensors = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(gatewayStatus);
        JSONArray array = jsonObject.getJSONArray("BinarySensors");
        for (int i = 0; i < array.length(); i++) {
            binarySensors.add(BinarySensor.parseJSON(array.getJSONObject(i)));
        }
        return binarySensors;
    }

    public void disArmSensor(String gatewaySerial, int nodeId) throws NetworkErrorException {
        String url = BASE_URL + "/main/disArmUserComponent?token=" + token.getToken() + "&user=" + token.getUser();
        String post = "gateway_id=" + gatewaySerial + "&node_id=" + nodeId;
        httpHelper.sendPost(url, post);
    }

    public void armSensor(String gatewaySerial, int nodeId) throws NetworkErrorException {
        String url = BASE_URL + "/main/reArmUserComponent?token=" + token.getToken() + "&user=" + token.getUser();
        String post = "gateway_id=" + gatewaySerial + "&node_id=" + nodeId;
        httpHelper.sendPost(url, post);
    }

    @Deprecated
    public List<Thermostat> getThermostats(String gatewaySerial) throws NetworkErrorException, JSONException {
        String gatewayStatus = getGatewayStatus(gatewaySerial);
        List<Thermostat> thermostats = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(gatewayStatus);
        JSONArray array = jsonObject.getJSONArray("Thermostats");
        for (int i = 0; i < array.length(); i++) {
            thermostats.add(Thermostat.parseJSON(array.getJSONObject(i)));
        }
        return thermostats;
    }
}