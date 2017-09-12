package org.openhab.binding.northqbinding.network;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.openhab.binding.northqbinding.network.HttpClient.Result;

class HttpHelper {
    private HttpClient httpClient;

    HttpHelper() {
        this.httpClient = new HttpClient();
    }

    String sendGet(String url) throws NetworkErrorException {
        try {
            return handleResponse(httpClient.doNetwork(url, "GET", ""));
        } catch (JSONException | IOException e) {
            throw new NetworkErrorException(e.getMessage(), e);
        }
    }

    String sendPost(String url, String post) throws NetworkErrorException {
        try {
            return handleResponse(httpClient.doNetwork(url, "POST", post));
        } catch (JSONException | IOException e) {
            throw new NetworkErrorException(e.getMessage(), e);
        }
    }

    private String handleResponse(Result result) throws NetworkErrorException, JSONException {
        if (result.getResponseCode() == 200) {
            String responseBody = result.getBody();
            if (responseBody.startsWith("[")) {
                return responseBody;
            } else {
                JSONObject jsonObject = new JSONObject(responseBody);
                if (!jsonObject.isNull("errors")) {
                    throw new NetworkErrorException(
                            "errors: " + jsonObject.getString("errors") + "\njsonbody: " + responseBody);
                } else if (!jsonObject.isNull("success")) {
                    Object success = jsonObject.get("success");
                    if ((success instanceof Boolean && ((Boolean) success))
                            || (success instanceof Integer && ((Integer) success) == 1)) {
                        return responseBody;
                    } else {
                        throw new NetworkErrorException("Unknown error. Body: " + responseBody);
                    }
                } else {
                    return responseBody;
                }
            }
        } else {
            throw new NetworkErrorException("ERROR: " + result.getBody() + " (" + result.getResponseCode() + ")");
        }
    }
}
