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
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
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
    private ItemChannelLinkRegistry itemChannelLinkRegistry;
    private ItemRegistry itemRegistry;
    private final VolatileStorageService volatileStorageService = new VolatileStorageService();
    private Bridge bridge;
    private Thing thing;
    private MockedHttpClient mockedHttpClient;

    @Before
    public void setUp() {
        registerService(volatileStorageService);
        // managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);
        // assertThat(managedThingProvider, is(notNullValue()));
        thingRegistry = getService(ThingRegistry.class, ThingRegistry.class);
        assertThat(thingRegistry, is(notNullValue()));
        itemChannelLinkRegistry = getService(ItemChannelLinkRegistry.class, ItemChannelLinkRegistry.class);
        assertThat(itemChannelLinkRegistry, is(notNullValue()));
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
        this.bridge = BridgeBuilder.create(THING_TYPE_BRIDGE, "1").withConfiguration(configuration).build();
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
        this.thing = thingRegistry.createThingOfType(thing.getThingTypeUID(),
                new ThingUID(thing.getThingTypeUID(), bridge.getUID(), thing.getUniqueId()), bridge.getUID(), "Thing1",
                configuration);
        assertThat(this.thing.getHandler(), is(nullValue()));
        thingRegistry.add(this.thing);
        waitForAssert(() -> assertThat(this.thing.getHandler(), is(notNullValue())));

        for (Channel c : this.thing.getChannels()) {
            String item = this.thing.getUID().toString().replace(":", "_").replace("-", "_") + "_" + c.getUID().getId();
            if (itemChannelLinkRegistry.getBoundChannels(item).size() == 0) {
                itemChannelLinkRegistry.add(new ItemChannelLink(item, c.getUID()));
            }
        }
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
        thingRegistry.remove(bridge.getUID());
        thingRegistry.remove(thing.getUID());
        unregisterService(volatileStorageService);
    }

    @Test
    public void initializationTest() {
        initializeThingAndBridge(new BinarySwitch(1, 20));
        assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
    }

    @Test
    public void updateBinarySwitchTest() {
        AtomicBoolean getThingsCalled = new AtomicBoolean(false);

        BinarySwitch binarySwitch = new BinarySwitch(1, 20);
        initializeThingAndBridge(binarySwitch);

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

        Item powerState = getItem(BINARY_SWITCH_SWITCH_CHANNEL);
        Item wattage = getItem(BINARY_SWITCH_WATTAGE_CHANNEL);
        waitForAssert(() -> assertThat(getThingsCalled.get(), is(true)));

    }

    private Item getItem(String channel) {
        String item = thing.getUID().toString().replace(":", "_").replace("-", "_") + "_" + channel;
        return itemRegistry.get(item);
    }
}
