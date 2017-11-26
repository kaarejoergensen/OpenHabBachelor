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
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingProvider;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.northqbinding.handler.NorthQBindingHandler;
import org.openhab.binding.northqbinding.handler.NorthQBridgeHandler;
import org.openhab.binding.northqbinding.models.BinarySwitch;
import org.openhab.binding.northqbinding.models.NorthQThing;
import org.openhab.binding.northqbinding.osgi.helper.MockedHttpClient;
import org.openhab.binding.northqbinding.osgi.helper.ReflectionHelper;

/**
 * Tests cases for {@link NorthQBindingHandler}.
 *
 * @author Kaare Joergensen - Initial contribution
 */
public class NorthQBindingHandlerOSGiTest extends JavaOSGiTest {
    private ManagedThingProvider managedThingProvider;
    private ThingRegistry thingRegistry;
    private ItemRegistry itemRegistry;
    private final VolatileStorageService volatileStorageService = new VolatileStorageService();
    private Bridge bridge;
    private Thing thing;
    private MockedHttpClient mockedHttpClient;

    @Before
    public void setUp() {
        registerService(volatileStorageService);
        managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);
        assertThat(managedThingProvider, is(notNullValue()));
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
        // this.bridge = BridgeBuilder.create(THING_TYPE_BRIDGE, "1").withConfiguration(configuration).build();
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
        managedThingProvider.remove(bridge.getUID());
        managedThingProvider.remove(thing.getUID());
        waitForAssert(() -> {
            assertThat(managedThingProvider.get(bridge.getUID()), is(nullValue()));
            assertThat(managedThingProvider.get(thing.getUID()), is(nullValue()));
        });
        unregisterService(volatileStorageService);
    }

    @Test
    public void initializationTest() {
        initializeThingAndBridge(new BinarySwitch(255, 20));
        assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
        waitForAssert(() -> {
            assertThat(getItem(BINARY_SWITCH_SWITCH_CHANNEL).getState(), is(OnOffType.ON));
            assertThat(getItem(BINARY_SWITCH_WATTAGE_CHANNEL).getState(), is(DecimalType.valueOf("20")));
        });
    }

    @Test
    public void updateBinarySwitchTest() {
        AtomicBoolean getThingsCalled = new AtomicBoolean(false);

        BinarySwitch binarySwitch = new BinarySwitch(255, 20);
        initializeThingAndBridge(binarySwitch);
        waitForAssert(() -> {
            assertThat(getItem(BINARY_SWITCH_SWITCH_CHANNEL).getState(), is(OnOffType.ON));
            assertThat(getItem(BINARY_SWITCH_WATTAGE_CHANNEL).getState(), is(DecimalType.valueOf("20")));
        });
        binarySwitch.setTurnedOn(false);
        binarySwitch.setWattage(0);
        mockedHttpClient = new MockedHttpClient() {
            @Override
            public Result get(String address) throws IOException {
                if (address.contains("getGatewayStatus")) {
                    getThingsCalled.set(true);
                    return new Result(createGatewayStatus(setNorthQAttributes(binarySwitch)), 200);
                }
                return super.get(address);
            }
        };
        NorthQBridgeHandler bridgeHandler = ((NorthQBridgeHandler) bridge.getHandler());
        waitForAssert(() -> ReflectionHelper.installHttpClientMock(bridgeHandler, mockedHttpClient));

        waitForAssert(() -> {
            assertThat(getThingsCalled.get(), is(true));
            assertThat(getItem(BINARY_SWITCH_SWITCH_CHANNEL).getState(), is(OnOffType.OFF));
            assertThat(getItem(BINARY_SWITCH_WATTAGE_CHANNEL).getState(), is(DecimalType.valueOf("0")));
        }, 20000, 50);

    }

    private Item getItem(String channel) {
        String item = thing.getUID().toString().replace(":", "_").replace("-", "_") + "_" + channel;
        waitForAssert(() -> assertThat(itemRegistry.get(item), is(notNullValue())));
        return itemRegistry.get(item);
    }
}
