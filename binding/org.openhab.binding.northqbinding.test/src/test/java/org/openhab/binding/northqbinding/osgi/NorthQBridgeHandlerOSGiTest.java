/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.northqbinding.osgi;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.openhab.binding.northqbinding.NorthQBindingBindingConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.ThingProvider;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.northqbinding.handler.NorthQBindingHandler;
import org.openhab.binding.northqbinding.handler.NorthQBridgeHandler;
import org.openhab.binding.northqbinding.models.BinarySensor;
import org.openhab.binding.northqbinding.models.BinarySensor.Sensor;
import org.openhab.binding.northqbinding.models.BinarySwitch;
import org.openhab.binding.northqbinding.models.ErrorResponse;
import org.openhab.binding.northqbinding.models.NorthQThing;
import org.openhab.binding.northqbinding.models.Thermostat;
import org.openhab.binding.northqbinding.osgi.helper.MockedHttpClient;
import org.openhab.binding.northqbinding.osgi.helper.ReflectionHelper;

/**
 * Tests cases for {@link NorthQBindingHandler}.
 *
 * @author Kaare Joergensen - Initial contribution
 */
public class NorthQBridgeHandlerOSGiTest extends JavaOSGiTest {
    private ManagedThingProvider managedThingProvider;
    private final VolatileStorageService volatileStorageService = new VolatileStorageService();
    private Bridge bridge;
    private MockedHttpClient mockedHttpClient;

    @Before
    public void setUp() {
        registerService(volatileStorageService);
        managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);
        assertThat(managedThingProvider, is(notNullValue()));
    }

    public void createBridge() {
        if (bridge != null && managedThingProvider.get(bridge.getUID()) != null) {
            managedThingProvider.remove(bridge.getUID());
        }
        Configuration configuration = new Configuration();
        configuration.put(EMAIL, "testemail@test.com");
        configuration.put(PASSWORD, "testPassword");
        this.bridge = BridgeBuilder.create(THING_TYPE_BRIDGE, "1").withConfiguration(configuration).build();
        assertThat(bridge.getHandler(), is(nullValue()));
        managedThingProvider.add(bridge);
        waitForAssert(() -> assertThat(bridge.getHandler(), is(notNullValue())));
        waitForAssert(() -> ReflectionHelper.installHttpClientMock((NorthQBridgeHandler) bridge.getHandler(),
                mockedHttpClient));
    }

    @After
    public void tearDown() {
        managedThingProvider.remove(bridge.getUID());
        unregisterService(volatileStorageService);
    }

    @Test
    public void authenticationUnsuccessfulTest() {
        mockedHttpClient = new MockedHttpClient() {
            @Override
            public Result post(String address, String body) throws IOException {
                if (address.endsWith("token/new.json")) {
                    return new Result(gson.toJson(new ErrorResponse(false, "Authentication error")), 200);
                }
                return super.post(address, body);
            }
        };
        createBridge();
        assertFalse(((NorthQBridgeHandler) bridge.getHandler()).onAuthenticationError());
        assertThat(bridge.getStatus(), is(ThingStatus.OFFLINE));
        assertThat(bridge.getStatusInfo().getStatusDetail(), is(ThingStatusDetail.CONFIGURATION_ERROR));
    }

    @Test
    public void authenticationSuccessfulTest() {
        // MockedHttpClient provides, by default, a working authentication method
        mockedHttpClient = new MockedHttpClient();
        createBridge();
        assertTrue(((NorthQBridgeHandler) bridge.getHandler()).onAuthenticationError());
        assertThat(bridge.getStatus(), is(ThingStatus.ONLINE));
    }

    @Test
    public void binarySwitchTest() {
        BinarySwitch binarySwitch = new BinarySwitch(1, 20);
        testNorthQThing(binarySwitch);

        mockedHttpClient = new MockedHttpClient() {
            @Override
            public Result post(String address, String body) throws IOException {
                if (address.contains("setBinaryValue")) {
                    return new Result("{ }", 200);
                }
                return super.post(address, body);
            }
        };
        waitForAssert(() -> ReflectionHelper.installHttpClientMock((NorthQBridgeHandler) bridge.getHandler(),
                mockedHttpClient));

        ((NorthQBridgeHandler) bridge.getHandler()).changeSwitchState(binarySwitch);
        BinarySwitch result = (BinarySwitch) ((NorthQBridgeHandler) bridge.getHandler())
                .getThingByUniqueId(binarySwitch.getUniqueId());
        assertThat(result.isTurnedOn(), is(!binarySwitch.isTurnedOn()));

        ((NorthQBridgeHandler) bridge.getHandler()).changeSwitchState(result);
        result = (BinarySwitch) ((NorthQBridgeHandler) bridge.getHandler())
                .getThingByUniqueId(binarySwitch.getUniqueId());
        assertThat(result.isTurnedOn(), is(binarySwitch.isTurnedOn()));
    }

    @Test
    public void binarySensorTest() {
        List<Sensor> sensors = new ArrayList<Sensor>();
        sensors.add(new Sensor(0, Sensor.Type.HUMIDITY.ordinal(), 50));
        sensors.add(new Sensor(0, Sensor.Type.lUMINANCE.ordinal(), 75));
        sensors.add(new Sensor(0, Sensor.Type.TEMPERATURE.ordinal(), 100));
        BinarySensor binarySensor = new BinarySensor(100, 1, 1, sensors);
        testNorthQThing(binarySensor);
    }

    @Test
    public void thermostatTest() {
        Thermostat thermostat = new Thermostat(100, 25);
        testNorthQThing(thermostat);
        List<Thermostat> thermostats = ((NorthQBridgeHandler) bridge.getHandler())
                .getThermostatsByRoomId(thermostat.getRoom());
        assertThat(thermostats, is(notNullValue()));
        assertThat(thermostats.size(), is(1));
        assertTrue(thermostats.get(0).isEqual(thermostat));
    }

    @Test
    public void testGatewayOffline() {
        mockedHttpClient = new MockedHttpClient() {
            @Override
            public Result get(String address) throws IOException {
                if (address.contains("getGatewayStatus")) {
                    return new Result(gson.toJson(new ErrorResponse(false, "Gateway offline", 1003)), 200);
                }
                return super.get(address);
            }
        };
        createBridge();
        assertThat(((NorthQBridgeHandler) bridge.getHandler()).onAuthenticationError(), is(true));
        assertThat(bridge.getStatus(), is(ThingStatus.ONLINE));
        ((NorthQBridgeHandler) bridge.getHandler()).getAllNorthQThings();
        assertThat(bridge.getStatus(), is(ThingStatus.OFFLINE));
        assertThat(bridge.getStatusInfo().getStatusDetail(), is(ThingStatusDetail.BRIDGE_OFFLINE));
    }

    private void testNorthQThing(NorthQThing thing) {
        thing.setNode_id(1);
        thing.setGateway("0000000001");
        thing.setName("TestThing");
        thing.setRead(System.currentTimeMillis());
        thing.setRoom(1);

        mockedHttpClient = new MockedHttpClient() {
            @Override
            public Result get(String address) throws IOException {
                if (address.contains("getGatewayStatus")) {
                    return new Result(createGatewayStatus(thing), 200);
                }
                return super.get(address);
            }
        };
        createBridge();
        assertTrue(((NorthQBridgeHandler) bridge.getHandler()).onAuthenticationError());
        assertThat(bridge.getStatus(), is(ThingStatus.ONLINE));

        List<NorthQThing> things = ((NorthQBridgeHandler) bridge.getHandler()).getAllNorthQThings();
        assertThat(things, is(notNullValue()));
        assertThat(things.size(), is(1));
        assertTrue(things.get(0).getClass().equals(thing.getClass()));
        NorthQThing result = things.get(0);
        assertTrue(result.isEqual(thing));
        NorthQThing resultFromMethod = ((NorthQBridgeHandler) bridge.getHandler())
                .getThingByUniqueId(thing.getUniqueId());
        assertThat(resultFromMethod, is(notNullValue()));
        assertThat(result, is(equalTo(resultFromMethod)));
    }
}
