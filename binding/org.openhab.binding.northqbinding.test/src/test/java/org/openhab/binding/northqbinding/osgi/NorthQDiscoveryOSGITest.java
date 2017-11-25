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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.ThingProvider;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.test.AsyncResultWrapper;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.northqbinding.discovery.NorthQDiscovery;
import org.openhab.binding.northqbinding.handler.NorthQBridgeHandler;
import org.openhab.binding.northqbinding.models.BinarySwitch;
import org.openhab.binding.northqbinding.models.NorthQThing;
import org.openhab.binding.northqbinding.models.Room;
import org.openhab.binding.northqbinding.osgi.helper.MockedHttpClient;
import org.openhab.binding.northqbinding.osgi.helper.ReflectionHelper;

/**
 * Tests cases for {@link NorthQDiscovery}.
 *
 * @author Kaare Joergensen - Initial contribution
 */
public class NorthQDiscoveryOSGITest extends JavaOSGiTest {
    private ManagedThingProvider managedThingProvider;
    private final VolatileStorageService volatileStorageService = new VolatileStorageService();
    private Bridge bridge;
    private NorthQDiscovery discoveryService;
    private DiscoveryListener discoveryListener;

    @Before
    public void setUp() {
        registerService(volatileStorageService);
        managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);
        assertThat(managedThingProvider, is(notNullValue()));
        createBridge();
        discoveryService = getService(DiscoveryService.class, NorthQDiscovery.class);
        assertThat(discoveryService, is(notNullValue()));
    }

    public void createBridge() {
        Configuration configuration = new Configuration();
        configuration.put(EMAIL, "testemail@test.com");
        configuration.put(PASSWORD, "testPassword");
        this.bridge = BridgeBuilder.create(THING_TYPE_BRIDGE, "1").withConfiguration(configuration).build();
        assertThat(bridge, is(notNullValue()));
        assertThat(bridge.getHandler(), is(nullValue()));
        managedThingProvider.add(bridge);
        waitForAssert(() -> assertThat(bridge.getHandler(), is(notNullValue())));
    }

    private void registerDiscoveryListener(DiscoveryListener discoveryListener) {
        unregisterCurrentDiscoveryListener();
        this.discoveryListener = discoveryListener;
        discoveryService.addDiscoveryListener(this.discoveryListener);
    }

    private void unregisterCurrentDiscoveryListener() {
        if (this.discoveryListener != null) {
            discoveryService.removeDiscoveryListener(this.discoveryListener);
        }
    }

    @After
    public void tearDown() {
        managedThingProvider.remove(bridge.getUID());
        unregisterService(volatileStorageService);
    }

    @Test
    public void northQThingRegistration() {
        NorthQThing thing = new BinarySwitch(0, 0);
        thing.setNode_id(1);
        thing.setGateway("0000000001");
        thing.setName("TestThing");
        thing.setRead(System.currentTimeMillis());
        thing.setRoom(1);

        AsyncResultWrapper<DiscoveryResult> resultWrapper = new AsyncResultWrapper<>();
        registerDiscoveryListener(new DiscoveryListener() {

            @Override
            public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
            }

            @Override
            public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
                resultWrapper.set(result);
            }

            @Override
            public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
                    Collection<ThingTypeUID> thingTypeUIDs) {
                return Collections.emptyList();
            }
        });

        Room room = new Room(1, "Test room", 0, "0000000001");
        installRoomMap(discoveryService, Collections.singletonMap(room.getId(), room));

        discoveryService.onThingAdded(thing);
        waitForAssert(() -> assertThat(resultWrapper.isSet(), is(true)));

        DiscoveryResult discoveryResult = resultWrapper.getWrappedObject();
        assertThat(discoveryResult.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(discoveryResult.getThingUID().toString(),
                is("northqbinding:binary-switch:1:" + thing.getUniqueId()));
        assertThat(discoveryResult.getThingTypeUID(), is(BINARY_SWITCH));
        assertThat(discoveryResult.getBridgeUID(), is(bridge.getUID()));
        assertThat(discoveryResult.getProperties().get(UNIQUE_ID), is(thing.getUniqueId()));
        assertThat(discoveryResult.getProperties().get(ROOM_ID),
                is(String.format("%d%s%s", room.getId(), ROOM_ID_SEPERATOR, room.getName())));
    }

    @Test
    public void startSearchIsCalled() {
        final AtomicBoolean searchHasBeenTriggered = new AtomicBoolean(false);

        MockedHttpClient mockedHttpClient = new MockedHttpClient() {
            @Override
            public Result get(String address) throws IOException {
                if (address.contains("getGatewayStatus")) {
                    searchHasBeenTriggered.set(true);
                }
                return super.get(address);
            }
        };
        waitForAssert(() -> ReflectionHelper.installHttpClientMock((NorthQBridgeHandler) bridge.getHandler(),
                mockedHttpClient));
        assertTrue(((NorthQBridgeHandler) bridge.getHandler()).onAuthenticationError());
        assertThat(bridge.getStatus(), is(ThingStatus.ONLINE));
        discoveryService.startScan();
        assertTrue(searchHasBeenTriggered.get());
    }

    private void installRoomMap(NorthQDiscovery discoveryService, Map<Integer, Room> roomMap) {
        waitForAssert(() -> {
            try {
                assertThat(roomMap, is(notNullValue()));
                Field roomMapField = NorthQDiscovery.class.getDeclaredField("roomMap");
                roomMapField.setAccessible(true);
                roomMapField.set(discoveryService, roomMap);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
                Assert.fail("Reflection error: " + e.getMessage());
            }
        });
    }
}
