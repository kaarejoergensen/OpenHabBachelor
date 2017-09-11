package network;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

class HttpHelper {

    private static final MediaType URLENCODED = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    final OkHttpClient httpClient;

    HttpHelper(OkHttpClient okHttpClient) {
        this.httpClient = okHttpClient;
    }

    private Request buildGet(String url) {
        return new Request.Builder().url(url).build();
    }

    private Request buildPost(String url, String post) {
        return new Request.Builder().url(url).post(RequestBody.create(URLENCODED, post)).build();
    }

    String sendGet(String url) throws NetworkErrorException {
        return handleResponse(buildGet(url));
    }

    String sendPost(String url, String post) throws NetworkErrorException {
        return handleResponse(buildPost(url, post));
    }

    private String handleResponse(Request request) throws NetworkErrorException {
        String responseBody = "";
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                responseBody = response.body().string();
                if (responseBody.startsWith("[")) {
                    return responseBody;
                } else {
                    JSONObject jsonObject = new JSONObject(responseBody);
                    if (!jsonObject.isNull("errors")) {
                        throw new NetworkErrorException(jsonObject.getString("errors"));
                    } else if (!jsonObject.isNull("success")) {
                        Object success = jsonObject.get("success");
                        if ((success instanceof Boolean && ((Boolean) success))
                                || (success instanceof Integer && ((Integer) success) == 1)) {
                            return responseBody;
                        } else {
                            throw  new NetworkErrorException("Unknown error, success false. Body: " + responseBody);
                        }
                    }
                }
            } else {
                throw new NetworkErrorException("ERROR: " + response.message() + " (" + response.code() + ")");
            }
        } catch (IOException | JSONException e) {
            throw new NetworkErrorException(e.getMessage(), e);
        }

        return responseBody;
    }
}
