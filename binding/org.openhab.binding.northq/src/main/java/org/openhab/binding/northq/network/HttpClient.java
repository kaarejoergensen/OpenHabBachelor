/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.northq.network;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
public class HttpClient {
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";

    private int timeout = 20000;

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Result get(String address) throws IOException {
        return doNetwork(address, GET, "");
    }

    public Result post(String address, String body) throws IOException {
        return doNetwork(address, POST, body);
    }

    public Result put(String address, String body) throws IOException {
        return doNetwork(address, PUT, body);
    }

    public Result delete(String address) throws IOException {
        return doNetwork(address, DELETE, "");
    }

    protected Result doNetwork(String address, String requestMethod, String body) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(address).openConnection();
        try {
            conn.setRequestMethod(requestMethod);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);

            if (body != null && !body.equals("")) {
                conn.setDoOutput(true);
                OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
                out.write(body);
                out.close();
            }

            InputStream in = new BufferedInputStream(conn.getInputStream());
            String output = convertStreamToString(in);
            return new Result(output, conn.getResponseCode());
        } finally {
            conn.disconnect();
        }
    }

    private static String convertStreamToString(InputStream is) {
        Scanner scan = new Scanner(is);
        try {
            return scan.useDelimiter("\\A").next();
        } catch (NoSuchElementException e) {
            return "";
        } finally {
            scan.close();
        }
    }

    public static class Result {
        private String body;
        private int responseCode;

        public Result(String body, int responseCode) {
            this.body = body;
            this.responseCode = responseCode;
        }

        public String getBody() {
            return body;
        }

        public int getResponseCode() {
            return responseCode;
        }
    }
}
