package network;

import models.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class APIManager {
    private static final String BASE_URL = "https://homemanager.tv";
    private HttpHelper httpHelper;
    private Token token;

    public APIManager(OkHttpClient okHttpClient) {
        this.httpHelper = new HttpHelper(okHttpClient);
    }

    public Boolean authenticate(String user, String pass) {
        String url = BASE_URL + "/token/new.json";
        Request request = httpHelper.buildPost(url, "username=" + user + "&password=" + pass);

        Boolean success = false;

        try (Response response = httpHelper.httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JSONObject body = new JSONObject(response.body().string());
                if (body.isNull("success") || body.getBoolean("success")) {
                    token = Token.parseJSON(body);
                    success = true;
                } else {
                    System.out.println("ERROR: " + body.getString("errors"));
                }
            } else {
                System.out.println("ERROR: " + response.message() + " (" + response.code() + ")");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return success;
    }

    public List<House> getHouses() {
        String url = BASE_URL + "/main/getCurrentUserHouses?token=" + token.getToken() + "&user=" + token.getUser();
        Request request = httpHelper.buildGet(url);
        List<House> houses = new ArrayList<>();
        try (Response response = httpHelper.httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JSONArray body = new JSONArray(response.body().string());
                for (int i = 0; i < body.length(); i++) {
                    houses.add(House.parseJSON(body.getJSONObject(i)));
                }
            } else {
                System.out.println("ERROR: " + response.message() + " (" + response.code() + ")");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return houses;
    }

    public List<Gateway> getGateways(int houseId) {
        String url = BASE_URL + "/main/getHouseGateways?token=" + token.getToken() + "&user=" + token.getUser() + "&house_id=" + houseId;
        Request request = httpHelper.buildGet(url);
        List<Gateway> gateways = new ArrayList<>();
        try (Response response = httpHelper.httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseText = response.body().string();
                if (responseText.startsWith("[")) {
                    JSONArray body = new JSONArray(responseText);
                    for (int i = 0; i < body.length(); i++) {
                        gateways.add(Gateway.parseJSON(body.getJSONObject(i)));
                    }
                } else {
                    JSONObject error = new JSONObject(responseText);
                    System.out.println("ERROR: " + error.getString("errors"));
                }
            } else {
                System.out.println("ERROR: " + response.message() + " (" + response.code() + ")");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return gateways;
    }

    public List<Room> getRooms(String gatewaySerial) {
        String url = BASE_URL + "/main/getRoomsStatus?token=" + token.getToken() + "&user=" + token.getUser() + "&gateway=" + gatewaySerial;
        Request request = httpHelper.buildGet(url);
        List<Room> rooms = new ArrayList<>();
        try (Response response = httpHelper.httpClient.newCall(request).execute()){
            if (response.isSuccessful()) {
                String responseText = response.body().string();
                if (responseText.startsWith("[")) {
                    JSONArray body = new JSONArray(responseText);
                    for (int i = 0; i < body.length(); i++) {
                        rooms.add(Room.parseJSON(body.getJSONObject(i)));
                    }
                } else {
                    JSONObject error = new JSONObject(responseText);
                    System.out.println("ERROR: " + error.getString("errors"));
                }
            } else {
                System.out.println("ERROR: " + response.message() + " (" + response.code() + ")");
            }
        } catch (IOException|JSONException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public String getGatewayStatus(String gatewaySerial) {
        String url = BASE_URL + "/main/getGatewayStatus?token=" + token.getToken() + "&user=" + token.getUser() + "&gateway=" + gatewaySerial;
        Request request = httpHelper.buildGet(url);

        try (Response response = httpHelper.httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                System.out.println("ERROR: " + response.message() + " (" + response.code() + ")");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public List<BinarySwitch> getSwitches(String gatewaySerial) {
        String gatewayStatus = getGatewayStatus(gatewaySerial);
        List<BinarySwitch> binarySwitches = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(gatewayStatus);
            JSONArray array = jsonObject.getJSONArray("BinarySwitches");
            for (int i = 0; i < array.length(); i++) {
                binarySwitches.add(BinarySwitch.parseJSON(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return binarySwitches;
    }

    public boolean changeSwitchState(String gatewaySerial, BinarySwitch binarySwitch) {
        boolean turnedOn = binarySwitch.isTurnedOn();
        String url = BASE_URL + "/main/setBinaryValue?token=" + token.getToken() + "&user=" + token.getUser();
        Request request = httpHelper.buildPost(url, "gateway=" + gatewaySerial + "&node_id=" +
                binarySwitch.getNode_id() + "&pos=" + (turnedOn ? 0 : 255));
        try (Response response = httpHelper.httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JSONObject body = new JSONObject(response.body().string());
                if (body.isNull("errors")) {
                    return true;
                } else  {
                    System.out.println("ERROR: " + body.getString("errors"));
                    return false;
                }
            } else {
                System.out.println("ERROR: " + response.message() + " (" + response.code() + ")");
            }
        } catch (IOException|JSONException e) {
            e.printStackTrace();
        }
        return turnedOn;
    }

    public boolean setRoomTemperature(String gatewaySerial, Room room, double newTemperature) {
        boolean success = false;
        String url = BASE_URL + "/main/setRoomTemperature?token=" + token.getToken() + "&user=" + token.getUser();
        Request request = httpHelper.buildPost(url, "gateway=" + gatewaySerial + "&room_id=" + room.getId()
                + "&temperature=" + newTemperature);
        try (Response response = httpHelper.httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JSONObject body = new JSONObject(response.body().string());
                if (body.isNull("errors")) {
                    success = true;
                } else  {
                    System.out.println("ERROR: " + body.getString("errors"));
                    success = false;
                }
            } else {
                System.out.println("ERROR: " + response.message() + " (" + response.code() + ")");
            }
        } catch (IOException|JSONException e) {
            e.printStackTrace();
        }
        return success;
    }

    public List<Thermostat> getThermostats(String gatewaySerial) {
        String gatewayStatus = getGatewayStatus(gatewaySerial);
        List<Thermostat> thermostats = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(gatewayStatus);
            JSONArray array = jsonObject.getJSONArray("Thermostats");
            for (int i = 0; i < array.length(); i++) {
                thermostats.add(Thermostat.parseJSON(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return thermostats;
    }
}
