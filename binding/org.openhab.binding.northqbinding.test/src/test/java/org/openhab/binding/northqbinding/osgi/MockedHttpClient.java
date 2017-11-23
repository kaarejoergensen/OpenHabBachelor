/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.northqbinding.osgi;

import java.io.IOException;

import org.openhab.binding.northqbinding.network.HttpClient;

public class MockedHttpClient extends HttpClient {
    @Override
    public Result post(String address, String body) throws IOException {
        if (address.endsWith("/token/new.json")) {
            return new Result(
                    "{\n" + "    \"token\": \"testToken\",\n" + "    \"user\": 1,\n" + "    \"success\": true\n" + "}",
                    200);
        } else {
            return null;
        }
    }

    @Override
    public Result get(String address) throws IOException {
        if (address.contains("getCurrentUserHouses")) {
            return new Result("[\n" + "    {\n" + "        \"UTC_offset\": 60,\n"
                    + "        \"name\": \"Test house\",\n" + "        \"country\": \"Denmark\",\n"
                    + "        \"region\": \"Europe/Copenhagen\",\n" + "        \"DST\": false,\n"
                    + "        \"type\": \"Apartment\",\n" + "        \"id\": 1\n" + "    }\n" + "]", 200);
        } else if (address.contains("getHouseGateways")) {
            return new Result("[\n" + "    {\n" + "        \"id\": 1,\n" + "        \"serial_nr\": \"0000000001\"\n"
                    + "    }\n" + "]", 200);

        } else if (address.contains("getRoomsStatus")) {
            return new Result("[ ]", 200);
        } else {
            return null;
        }
    }
}
