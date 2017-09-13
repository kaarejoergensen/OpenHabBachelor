package network;

import exceptions.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import models.*;
import network.HttpClient.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class QStickBridge {
    private static final String BASE_URL = "https://homemanager.tv";
    private HttpClient httpClient;
    private Token token;
    private Gson gson;

    public QStickBridge(String user, String pass) throws APIException, IOException {
        this.httpClient = new HttpClient();
        this.gson = new Gson();
        authenticate(user, pass);
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
        String url = BASE_URL + "/main/getHouseGateways?token=" + token.getToken() + "&user=" + token.getUser() + "&house_id=" + houseId;
        Result result = httpClient.get(url);
        try {
            handleErrors(result);
        } catch (NoActiveGatewaysException e) {
            return new ArrayList<>();
        }
        return gson.fromJson(result.getBody(), Gateway.gsonType);
    }

    public List<Room> getRooms(String gatewaySerial) throws APIException, IOException {
        String url = BASE_URL + "/main/getRoomsStatus?token=" + token.getToken() + "&user=" + token.getUser() + "&gateway=" + gatewaySerial;
        Result result = httpClient.get(url);
        try {
            handleErrors(result);
        } catch (NoActiveRoomsException e) {
            return new ArrayList<>();
        }
        return gson.fromJson(result.getBody(), Room.gsonType);
    }

    public String getGatewayStatus(String gatewaySerial) throws APIException, IOException {
        String url = BASE_URL + "/main/getGatewayStatus?token=" + token.getToken() + "&user=" + token.getUser() + "&gateway=" + gatewaySerial;
        Result result = httpClient.get(url);
        handleErrors(result);
        return result.getBody();
    }

    public List<BinarySwitch> getSwitches(String gatewaySerial) throws APIException, IOException {
        JsonObject jsonObject = (JsonObject) new JsonParser().parse(getGatewayStatus(gatewaySerial));
        return gson.fromJson(jsonObject.getAsJsonArray("BinarySwitches"), BinarySwitch.gsonType);
    }

    public void changeSwitchState(String gatewaySerial, BinarySwitch binarySwitch) throws IOException {
        boolean turnedOn = binarySwitch.isTurnedOn();
        String url = BASE_URL + "/main/setBinaryValue?token=" + token.getToken() + "&user=" + token.getUser();
        String post = "gateway=" + gatewaySerial + "&node_id=" + binarySwitch.getNode_id() + "&pos=" + (turnedOn ? 0 : 255);
        httpClient.post(url, post);
    }

    public void setRoomTemperature(String gatewaySerial, Room room, double newTemperature) throws IOException {
        String url = BASE_URL + "/main/setRoomTemperature?token=" + token.getToken() + "&user=" + token.getUser();
        String post = "gateway=" + gatewaySerial + "&room_id=" + room.getId() + "&temperature=" + newTemperature;
        httpClient.post(url, post);
    }

    public List<BinarySensor> getBinarySensors(String gatewaySerial) throws APIException, IOException {
        JsonObject jsonObject = (JsonObject) new JsonParser().parse(getGatewayStatus(gatewaySerial));
        return gson.fromJson(jsonObject.getAsJsonArray("BinarySensors"), BinarySensor.gsonType);
    }

    public void disArmSensor(String gatewaySerial, int nodeId) throws IOException {
        String url = BASE_URL + "/main/disArmUserComponent?token=" + token.getToken() + "&user=" + token.getUser();
        String post = "gateway_id=" + gatewaySerial + "&node_id=" + nodeId;
        httpClient.post(url, post);
    }

    public void armSensor(String gatewaySerial, int nodeId) throws IOException {
        String url = BASE_URL + "/main/reArmUserComponent?token=" + token.getToken() + "&user=" + token.getUser();
        String post = "gateway_id=" + gatewaySerial + "&node_id=" + nodeId;
        httpClient.post(url, post);
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