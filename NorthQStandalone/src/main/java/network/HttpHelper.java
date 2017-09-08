package network;

import okhttp3.*;

class HttpHelper {

    private static final MediaType JSON = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    final OkHttpClient httpClient;

    HttpHelper(OkHttpClient okHttpClient) {
        this.httpClient = okHttpClient;
    }

    Request buildGet(String url) {
        return new Request.Builder().url(url).build();
    }

    Request buildPost(String url, String post) {
        return new Request.Builder().url(url).post(RequestBody.create(JSON, post)).build();
    }
}
