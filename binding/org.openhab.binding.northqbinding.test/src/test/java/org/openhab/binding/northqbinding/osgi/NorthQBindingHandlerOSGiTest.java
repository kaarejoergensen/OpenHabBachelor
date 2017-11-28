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
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.openhab.binding.northqbinding.NorthQBindingBindingConstants.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
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
@FixMethodOrder(MethodSorters.JVM)
public class NorthQBindingHandlerOSGiTest extends JavaOSGiTest {
    private ThingRegistry thingRegistry;
    private ItemRegistry itemRegistry;
    private final VolatileStorageService volatileStorageService = new VolatileStorageService();
    private Bridge bridge;
    private Thing thing;
    private MockedHttpClient mockedHttpClient;

    @Before
    public void setUp() {
        registerService(volatileStorageService);
        thingRegistry = getService(ThingRegistry.class, ThingRegistry.class);
        assertThat(thingRegistry, is(notNullValue()));
        itemRegistry = getService(ItemRegistry.class, ItemRegistry.class);
        assertThat(itemRegistry, is(notNullValue()));
    }

    private void initializeThingAndBridge(NorthQThing thing) {
        final NorthQThing finalThing = setNorthQAttributes(thing);
        mockedHttpClient = new MockedHttpClient() {
            @Override
            public Result get(String address) throws IOException {
                if (address.contains("getGatewayStatus")) {
                    return new Result(createGatewayStatus(finalThing), 200);
                }
                return super.get(address);
            }
        };
        createBridge();
        createThing(finalThing);
    }

    private void createBridge() {
        Configuration configuration = new Configuration();
        configuration.put(EMAIL, "testemail@test.com");
        configuration.put(PASSWORD, "testPassword");
        ThingUID thingUID = new ThingUID(THING_TYPE_BRIDGE.getBindingId(), THING_TYPE_BRIDGE.getId(), "1");
        this.bridge = (Bridge) thingRegistry.createThingOfType(THING_TYPE_BRIDGE, thingUID, null, "bridge",
                configuration);
        assertThat(bridge.getHandler(), is(nullValue()));
        thingRegistry.add(bridge);
        waitForAssert(() -> assertThat(bridge.getHandler(), is(notNullValue())));
        waitForAssert(() -> ReflectionHelper.installHttpClientMock((NorthQBridgeHandler) bridge.getHandler(),
                mockedHttpClient));
        assertTrue(((NorthQBridgeHandler) bridge.getHandler()).onAuthenticationError());
        assertThat(bridge.getStatus(), is(ThingStatus.ONLINE));
        removeDiscoveryServiceHandler((NorthQBridgeHandler) bridge.getHandler());
    }

    private void createThing(NorthQThing thing) {
        Configuration configuration = new Configuration();
        configuration.put(UNIQUE_ID, thing.getUniqueId());
        configuration.put(ROOM_ID, String.valueOf(thing.getRoom()));
        ThingUID thingUID = new ThingUID(thing.getThingTypeUID(), bridge.getUID(), thing.getUniqueId());
        this.thing = thingRegistry.createThingOfType(thing.getThingTypeUID(), thingUID, bridge.getUID(), "Thing",
                configuration);
        assertThat(this.thing.getHandler(), is(nullValue()));
        thingRegistry.add(this.thing);
        waitForAssert(() -> assertThat(this.thing.getHandler(), is(notNullValue())));
    }

    private NorthQThing setNorthQAttributes(NorthQThing thing) {
        thing.setNode_id(1);
        thing.setGateway("0000000001");
        thing.setName("TestThing");
        thing.setRead(System.currentTimeMillis());
        thing.setRoom(1);

        return thing;
    }

    @After
    public void tearDown() {
        Collection<Thing> things = thingRegistry.getAll();
        things.stream().forEach(t -> thingRegistry.remove(t.getUID()));
        waitForAssert(() -> {
            things.stream().forEach(t -> assertThat(thingRegistry.get(t.getUID()), is(nullValue())));
        });
        unregisterService(volatileStorageService);
    }

    @Test
    public void initializationTest() {
        initializeThingAndBridge(new BinarySwitch(255, 20));
        waitForAssert(() -> {
            assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
            assertThat(getItem(BINARY_SWITCH_SWITCH_CHANNEL).getState(), is(OnOffType.ON));
            assertThat(getItem(BINARY_SWITCH_WATTAGE_CHANNEL).getState(), is(DecimalType.valueOf("20")));
        });
    }

    @Test
    public void bridgeOfflineTest() {
        initializeThingAndBridge(new BinarySwitch(255, 20));
        waitForAssert(() -> {
            assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
            assertThat(getItem(BINARY_SWITCH_SWITCH_CHANNEL).getState(), is(OnOffType.ON));
            assertThat(getItem(BINARY_SWITCH_WATTAGE_CHANNEL).getState(), is(DecimalType.valueOf("20")));
        });
        mockedHttpClient = new MockedHttpClient() {
            @Override
            public Result get(String address) throws IOException {
                if (address.contains("getGatewayStatus")) {
                    return new Result(gson.toJson(new ErrorResponse(false, "Gateway offline", 1003)), 200);
                }
                return super.get(address);
            }
        };
        waitForAssert(() -> ReflectionHelper.installHttpClientMock((NorthQBridgeHandler) bridge.getHandler(),
                mockedHttpClient));

        ((NorthQBridgeHandler) bridge.getHandler()).getAllNorthQThings();
        waitForAssert(() -> {
            assertThat(bridge.getStatus(), is(ThingStatus.OFFLINE));
            assertThat(bridge.getStatusInfo().getStatusDetail(), is(ThingStatusDetail.BRIDGE_OFFLINE));
            assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));
            assertThat(thing.getStatusInfo().getStatusDetail(), is(ThingStatusDetail.BRIDGE_OFFLINE));
        });
    }

    @Test
    public void bridgeRemovedTest() {
        initializeThingAndBridge(new BinarySwitch(255, 20));
        waitForAssert(() -> {
            assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
            assertThat(getItem(BINARY_SWITCH_SWITCH_CHANNEL).getState(), is(OnOffType.ON));
            assertThat(getItem(BINARY_SWITCH_WATTAGE_CHANNEL).getState(), is(DecimalType.valueOf("20")));
        });

        thingRegistry.remove(bridge.getUID());
        waitForAssert(() -> {
            assertThat(thingRegistry.get(bridge.getUID()), is(nullValue()));
            assertThat(thingRegistry.get(thing.getUID()), is(nullValue()));
            assertThat(thingRegistry.getAll().size(), is(0));
        });
    }

    @Test
    public void updateBinarySwitchTest() {
        BinarySwitch binarySwitch = new BinarySwitch(255, 20);
        Runnable assertation = () -> {
            assertThat(getItem(BINARY_SWITCH_SWITCH_CHANNEL).getState(), is(OnOffType.ON));
            assertThat(getItem(BINARY_SWITCH_WATTAGE_CHANNEL).getState(), is(DecimalType.valueOf("20")));
        };

        BinarySwitch updatedSwitch = new BinarySwitch(0, 0);
        Runnable updatedAssertation = () -> {
            assertThat(getItem(BINARY_SWITCH_SWITCH_CHANNEL).getState(), is(OnOffType.OFF));
            assertThat(getItem(BINARY_SWITCH_WATTAGE_CHANNEL).getState(), is(DecimalType.valueOf("0")));
        };

        testUpdate(binarySwitch, updatedSwitch, assertation, updatedAssertation);
    }

    @Test
    public void updateBinarySensorTest() {
        List<Sensor> sensors = new ArrayList<Sensor>();
        sensors.add(new Sensor(0, 1, 50));
        sensors.add(new Sensor(0, 3, 75));
        sensors.add(new Sensor(0, 5, 100));
        BinarySensor binarySensor = new BinarySensor(100, 255, 1, sensors);
        Runnable assertation = () -> {
            assertThat(getItem(BINARY_SENSOR_ARM_CHANNEL).getState(), is(OnOffType.ON));
            assertThat(getItem(BINARY_SENSOR_TRIGGERED_CHANNEL).getState(), is(OnOffType.ON));
            assertThat(getItem(BINARY_SENSOR_TEMP_CHANNEL).getState(), is(DecimalType.valueOf("50")));
            assertThat(getItem(BINARY_SENSOR_LUMINANCE_CHANNEL).getState(), is(DecimalType.valueOf("75")));
            assertThat(getItem(BINARY_SENSOR_HUMIDITY_CHANNEL).getState(), is(DecimalType.valueOf("100")));
        };

        sensors = new ArrayList<Sensor>();
        sensors.add(new Sensor(0, 1, 0));
        sensors.add(new Sensor(0, 3, 25));
        sensors.add(new Sensor(0, 5, 35));
        BinarySensor updatedBinarySensor = new BinarySensor(100, 0, 0, sensors);
        Runnable updatedAssertation = () -> {
            assertThat(getItem(BINARY_SENSOR_ARM_CHANNEL).getState(), is(OnOffType.OFF));
            assertThat(getItem(BINARY_SENSOR_TRIGGERED_CHANNEL).getState(), is(OnOffType.OFF));
            assertThat(getItem(BINARY_SENSOR_TEMP_CHANNEL).getState(), is(DecimalType.valueOf("0")));
            assertThat(getItem(BINARY_SENSOR_LUMINANCE_CHANNEL).getState(), is(DecimalType.valueOf("25")));
            assertThat(getItem(BINARY_SENSOR_HUMIDITY_CHANNEL).getState(), is(DecimalType.valueOf("35")));
        };

        testUpdate(binarySensor, updatedBinarySensor, assertation, updatedAssertation);
    }

    @Test
    public void updateThermostatTest() {
        Thermostat thermostat = new Thermostat(100, 25);
        Runnable assertation = () -> {
            assertThat(getItem(THERMOSTAT_TEMP_CHANNEL).getState(), is(DecimalType.valueOf("25")));
        };

        Thermostat updatedThermostat = new Thermostat(100, 5);
        Runnable updatedAssertation = () -> {
            assertThat(getItem(THERMOSTAT_TEMP_CHANNEL).getState(), is(DecimalType.valueOf("5")));
        };

        testUpdate(thermostat, updatedThermostat, assertation, updatedAssertation);
    }

    @Test
    public void sendCommandSuccessfullyBinarySwitchTest() {
        AtomicBoolean changeSwitchStateCalled = new AtomicBoolean(false);

        BinarySwitch binarySwitch = new BinarySwitch(255, 20);
        initializeThingAndBridge(binarySwitch);

        waitForAssert(() -> {
            assertThat(getItem(BINARY_SWITCH_SWITCH_CHANNEL).getState(), is(OnOffType.ON));
            assertThat(getItem(BINARY_SWITCH_WATTAGE_CHANNEL).getState(), is(DecimalType.valueOf("20")));
        });

        mockedHttpClient = new MockedHttpClient() {
            @Override
            public Result post(String address, String body) throws IOException {
                if (address.contains("setBinaryValue")) {
                    changeSwitchStateCalled.set(true);
                    return new Result(gson.toJson(new ErrorResponse(true)), 200);
                }
                return super.post(address, body);
            }
        };
        waitForAssert(() -> ReflectionHelper.installHttpClientMock((NorthQBridgeHandler) bridge.getHandler(),
                mockedHttpClient));

        postCommand(binarySwitch, BINARY_SWITCH_SWITCH_CHANNEL, OnOffType.OFF);
        waitForAssert(() -> {
            assertThat(changeSwitchStateCalled.get(), is(true));
            assertThat(getItem(BINARY_SWITCH_SWITCH_CHANNEL).getState(), is(OnOffType.OFF));
        });

        changeSwitchStateCalled.set(false);
        postCommand(binarySwitch, BINARY_SWITCH_SWITCH_CHANNEL, OnOffType.ON);
        waitForAssert(() -> {
            assertThat(changeSwitchStateCalled.get(), is(true));
            assertThat(getItem(BINARY_SWITCH_SWITCH_CHANNEL).getState(), is(OnOffType.ON));
        });
    }

    @Test
    public void sendCommandSuccessfullyBinarySensorTest() {
        AtomicBoolean disarmCalled = new AtomicBoolean(false);
        AtomicBoolean armCalled = new AtomicBoolean(false);

        List<Sensor> sensors = new ArrayList<Sensor>();
        sensors.add(new Sensor(0, 1, 50));
        sensors.add(new Sensor(0, 3, 75));
        sensors.add(new Sensor(0, 5, 100));
        BinarySensor binarySensor = new BinarySensor(100, 255, 1, sensors);
        initializeThingAndBridge(binarySensor);

        waitForAssert(() -> {
            assertThat(getItem(BINARY_SENSOR_ARM_CHANNEL).getState(), is(OnOffType.ON));
            assertThat(getItem(BINARY_SENSOR_TRIGGERED_CHANNEL).getState(), is(OnOffType.ON));
            assertThat(getItem(BINARY_SENSOR_TEMP_CHANNEL).getState(), is(DecimalType.valueOf("50")));
            assertThat(getItem(BINARY_SENSOR_LUMINANCE_CHANNEL).getState(), is(DecimalType.valueOf("75")));
            assertThat(getItem(BINARY_SENSOR_HUMIDITY_CHANNEL).getState(), is(DecimalType.valueOf("100")));
        });

        mockedHttpClient = new MockedHttpClient() {
            @Override
            public Result post(String address, String body) throws IOException {
                if (address.contains("disArmUserComponent")) {
                    disarmCalled.set(true);
                    return new Result(gson.toJson(new ErrorResponse(true)), 200);
                } else if (address.contains("reArmUserComponent")) {
                    armCalled.set(true);
                    return new Result(gson.toJson(new ErrorResponse(true)), 200);
                }
                return super.post(address, body);
            }
        };
        waitForAssert(() -> ReflectionHelper.installHttpClientMock((NorthQBridgeHandler) bridge.getHandler(),
                mockedHttpClient));

        postCommand(binarySensor, BINARY_SENSOR_ARM_CHANNEL, OnOffType.OFF);
        waitForAssert(() -> {
            assertThat(disarmCalled.get(), is(true));
            assertThat(getItem(BINARY_SENSOR_ARM_CHANNEL).getState(), is(OnOffType.OFF));
            assertThat(getItem(BINARY_SENSOR_TRIGGERED_CHANNEL).getState(), is(OnOffType.OFF));
        });

        postCommand(binarySensor, BINARY_SENSOR_ARM_CHANNEL, OnOffType.ON);
        waitForAssert(() -> {
            assertThat(armCalled.get(), is(true));
            assertThat(getItem(BINARY_SENSOR_ARM_CHANNEL).getState(), is(OnOffType.ON));
            assertThat(getItem(BINARY_SENSOR_TRIGGERED_CHANNEL).getState(), is(OnOffType.ON));
        });
    }

    @Test
    public void sendCommandSuccessfullyThermostatTest() {
        AtomicBoolean changeTemperatureCalled = new AtomicBoolean(false);

        Thermostat thermostat = new Thermostat(100, 25);
        initializeThingAndBridge(thermostat);

        waitForAssert(() -> {
            assertThat(getItem(THERMOSTAT_TEMP_CHANNEL).getState(), is(DecimalType.valueOf("25")));
        });

        mockedHttpClient = new MockedHttpClient() {
            @Override
            public Result post(String address, String body) throws IOException {
                if (address.contains("setRoomTemperature")) {
                    changeTemperatureCalled.set(true);
                    return new Result(gson.toJson(new ErrorResponse(true)), 200);
                }
                return super.post(address, body);
            }
        };
        waitForAssert(() -> ReflectionHelper.installHttpClientMock((NorthQBridgeHandler) bridge.getHandler(),
                mockedHttpClient));

        postCommand(thermostat, THERMOSTAT_TEMP_CHANNEL, DecimalType.valueOf("5"));
        waitForAssert(() -> {
            assertThat(changeTemperatureCalled.get(), is(true));
            assertThat(getItem(THERMOSTAT_TEMP_CHANNEL).getState(), is(DecimalType.valueOf("5")));
        });
    }

    @Test
    public void sendCommandUnSuccessfullyBinarySwitchTest() {
        AtomicBoolean changeSwitchStateCalled = new AtomicBoolean(false);

        BinarySwitch binarySwitch = new BinarySwitch(255, 20);
        initializeThingAndBridge(binarySwitch);

        waitForAssert(() -> {
            assertThat(getItem(BINARY_SWITCH_SWITCH_CHANNEL).getState(), is(OnOffType.ON));
            assertThat(getItem(BINARY_SWITCH_WATTAGE_CHANNEL).getState(), is(DecimalType.valueOf("20")));
        });

        mockedHttpClient = new MockedHttpClient() {
            @Override
            public Result post(String address, String body) throws IOException {
                if (address.contains("setBinaryValue")) {
                    changeSwitchStateCalled.set(true);
                    return new Result(gson.toJson(new ErrorResponse(false)), 200);
                }
                return super.post(address, body);
            }
        };
        waitForAssert(() -> ReflectionHelper.installHttpClientMock((NorthQBridgeHandler) bridge.getHandler(),
                mockedHttpClient));

        postCommand(binarySwitch, BINARY_SWITCH_SWITCH_CHANNEL, OnOffType.OFF);
        waitForAssert(() -> {
            assertThat(changeSwitchStateCalled.get(), is(true));
            assertThat(getItem(BINARY_SWITCH_SWITCH_CHANNEL).getState(), is(OnOffType.ON));
        });

    }

    @Test
    public void sendCommandUnSuccessfullyBinarySensorTest() {
        AtomicBoolean disarmCalled = new AtomicBoolean(false);

        List<Sensor> sensors = new ArrayList<Sensor>();
        sensors.add(new Sensor(0, 1, 50));
        sensors.add(new Sensor(0, 3, 75));
        sensors.add(new Sensor(0, 5, 100));
        BinarySensor binarySensor = new BinarySensor(100, 255, 1, sensors);
        initializeThingAndBridge(binarySensor);

        waitForAssert(() -> {
            assertThat(getItem(BINARY_SENSOR_ARM_CHANNEL).getState(), is(OnOffType.ON));
            assertThat(getItem(BINARY_SENSOR_TRIGGERED_CHANNEL).getState(), is(OnOffType.ON));
            assertThat(getItem(BINARY_SENSOR_TEMP_CHANNEL).getState(), is(DecimalType.valueOf("50")));
            assertThat(getItem(BINARY_SENSOR_LUMINANCE_CHANNEL).getState(), is(DecimalType.valueOf("75")));
            assertThat(getItem(BINARY_SENSOR_HUMIDITY_CHANNEL).getState(), is(DecimalType.valueOf("100")));
        });

        mockedHttpClient = new MockedHttpClient() {
            @Override
            public Result post(String address, String body) throws IOException {
                if (address.contains("disArmUserComponent")) {
                    disarmCalled.set(true);
                    return new Result(gson.toJson(new ErrorResponse(false)), 200);
                }
                return super.post(address, body);
            }
        };
        waitForAssert(() -> ReflectionHelper.installHttpClientMock((NorthQBridgeHandler) bridge.getHandler(),
                mockedHttpClient));

        postCommand(binarySensor, BINARY_SENSOR_ARM_CHANNEL, OnOffType.OFF);
        waitForAssert(() -> {
            assertThat(disarmCalled.get(), is(true));
            assertThat(getItem(BINARY_SENSOR_ARM_CHANNEL).getState(), is(OnOffType.ON));
            assertThat(getItem(BINARY_SENSOR_TRIGGERED_CHANNEL).getState(), is(OnOffType.ON));
        });
    }

    @Test
    public void sendCommandUnSuccessfullyThermostatTest() {
        AtomicBoolean changeTemperatureCalled = new AtomicBoolean(false);

        Thermostat thermostat = new Thermostat(100, 25);
        initializeThingAndBridge(thermostat);

        waitForAssert(() -> {
            assertThat(getItem(THERMOSTAT_TEMP_CHANNEL).getState(), is(DecimalType.valueOf("25")));
        });

        mockedHttpClient = new MockedHttpClient() {
            @Override
            public Result post(String address, String body) throws IOException {
                if (address.contains("setRoomTemperature")) {
                    changeTemperatureCalled.set(true);
                    return new Result(gson.toJson(new ErrorResponse(false)), 200);
                }
                return super.post(address, body);
            }
        };
        waitForAssert(() -> ReflectionHelper.installHttpClientMock((NorthQBridgeHandler) bridge.getHandler(),
                mockedHttpClient));

        postCommand(thermostat, THERMOSTAT_TEMP_CHANNEL, DecimalType.valueOf("5"));
        waitForAssert(() -> {
            assertThat(changeTemperatureCalled.get(), is(true));
            assertThat(getItem(THERMOSTAT_TEMP_CHANNEL).getState(), is(DecimalType.valueOf("25")));
        });
    }

    private void postCommand(NorthQThing northQThing, String channel, Command command) {
        Item item = getItem(channel);

        EventPublisher eventPublisher = getService(EventPublisher.class);
        assertThat(eventPublisher, is(notNullValue()));

        eventPublisher.post(ItemEventFactory.createCommandEvent(item.getUID(), command));
    }

    private void testUpdate(NorthQThing originalThing, NorthQThing updatedThing, Runnable originalAssertation,
            Runnable updatedAssertation) {
        AtomicBoolean getThingsCalled = new AtomicBoolean(false);

        initializeThingAndBridge(originalThing);
        waitForAssert(originalAssertation);

        mockedHttpClient = new MockedHttpClient() {
            @Override
            public Result get(String address) throws IOException {
                if (address.contains("getGatewayStatus")) {
                    getThingsCalled.set(true);
                    return new Result(createGatewayStatus(setNorthQAttributes(updatedThing)), 200);
                }
                return super.get(address);
            }
        };
        NorthQBridgeHandler bridgeHandler = ((NorthQBridgeHandler) bridge.getHandler());
        waitForAssert(() -> ReflectionHelper.installHttpClientMock(bridgeHandler, mockedHttpClient));
        bridgeHandler.startAutomaticRefresh();

        waitForAssert(() -> {
            assertThat(getThingsCalled.get(), is(true));
        }, 20000, 50);
        waitForAssert(updatedAssertation);
    }

    private Item getItem(String channel) {
        String item = thing.getUID().toString().replace(":", "_").replace("-", "_") + "_" + channel;
        waitForAssert(() -> assertThat(itemRegistry.get(item), is(notNullValue())));
        return itemRegistry.get(item);
    }

    private void removeDiscoveryServiceHandler(NorthQBridgeHandler bridgeHandler) {
        try {
            Field handlersField = NorthQBridgeHandler.class.getDeclaredField("handlers");
            handlersField.setAccessible(true);
            handlersField.set(bridgeHandler, new HashSet<>());
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            Assert.fail("Reflection error: " + e.getMessage());
        }
    }
}
