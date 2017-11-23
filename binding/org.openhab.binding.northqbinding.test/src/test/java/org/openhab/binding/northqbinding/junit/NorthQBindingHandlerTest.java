/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.northqbinding.junit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.openhab.binding.northqbinding.NorthQBindingBindingConstants.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openhab.binding.northqbinding.handler.NorthQBindingHandler;
import org.openhab.binding.northqbinding.handler.NorthQBridgeHandler;

/**
 * Tests cases for {@link NorthQBindingHandler}. The tests provide mocks for supporting entities using Mockito.
 *
 * @author Kaare Joergensen - Initial contribution
 */
public class NorthQBindingHandlerTest {

    private NorthQBindingHandler handler;

    @Mock
    private ThingHandlerCallback callback;

    @Mock
    private Thing thing;

    @Mock
    private Bridge bridge;

    @Mock
    private NorthQBridgeHandler northQBridgeHandler;

    @Before
    public void setUp() {
        initMocks(this);
        handler = spy(new NorthQBindingHandler(thing));
        handler.setCallback(callback);
        initializeBridge();
        initializeThing();
    }

    private void initializeThing() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(ROOM_ID, "1" + ROOM_ID_SEPERATOR + "Test Room");
        thingConfiguration.put(UNIQUE_ID, "testID");
        when(thing.getConfiguration()).thenReturn(thingConfiguration);
    }

    private void initializeBridge() {
        doReturn(ThingStatus.ONLINE).when(bridge).getStatus();
        doReturn(bridge).when(handler).getBridge();
        doReturn(northQBridgeHandler).when(handler).getBridgeHandler();
    }

    @Test
    public void initializeShouldCallTheCallback() {
        // we expect the handler#initialize method to call the callback during execution and
        // pass it the thing and a ThingStatusInfo object containing the ThingStatus of the thing.
        handler.initialize();

        // the argument captor will capture the argument of type ThingStatusInfo given to the
        // callback#statusUpdated method.
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);

        // verify the interaction with the callback and capture the ThingStatusInfo argument:
        verify(callback).statusUpdated(eq(thing), statusInfoCaptor.capture());
        // assert that the ThingStatusInfo given to the callback was build with the ONLINE status:
        ThingStatusInfo thingStatusInfo = statusInfoCaptor.getValue();
        assertThat(thingStatusInfo.getStatus(), is(equalTo(ThingStatus.ONLINE)));
    }
}
