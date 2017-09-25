/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.northqbinding.discovery;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.northqbinding.NorthQBindingBindingConstants;
import org.openhab.binding.northqbinding.handler.BindingHandlerInterface;
import org.openhab.binding.northqbinding.handler.NorthQBindingHandler;
import org.openhab.binding.northqbinding.handler.NorthQBridgeHandler;
import org.openhab.binding.northqbinding.models.NorthQThing;
import org.openhab.binding.northqbinding.models.Room;
import org.openhab.binding.northqbinding.models.Thermostat;

/**
 *
 * @author Kaare Joergensen - Initial contribution
 */
public class NorthQDiscovery extends AbstractDiscoveryService implements BindingHandlerInterface {
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;

    private NorthQBridgeHandler bridgeHandler;
    private Map<Integer, Room> roomMap = new HashMap<>();

    public NorthQDiscovery(NorthQBridgeHandler bridgeHandler) throws IllegalArgumentException {
        super(NorthQBindingHandler.SUPPORTED_THING_TYPES, DISCOVER_TIMEOUT_SECONDS);
        this.bridgeHandler = bridgeHandler;
    }

    public void activate() {
        bridgeHandler.addHandler(this);
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime());
        bridgeHandler.removeHandler(this);
    }

    @Override
    protected void modified(Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    protected void startScan() {
        bridgeHandler.updateHousesAndGateways();
        roomMap = bridgeHandler.getAllRooms().stream().collect(Collectors.toMap(Room::getId, Function.identity()));
        List<NorthQThing> things = bridgeHandler.getAllNorthQThings();
        for (NorthQThing thing : things) {
            if (thingActive(thing)) {
                addNorthQThing(thing);
            }
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    private boolean thingActive(NorthQThing thing) {
        Date lastRead = new Date(thing.getRead() * 1000L);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -8);
        Date yesterday = calendar.getTime();
        return yesterday.before(lastRead);
    }

    @Override
    protected void startBackgroundDiscovery() {
        scheduler.schedule(() -> {
            startScan();
        }, 1, TimeUnit.SECONDS);
    }

    private void addNorthQThing(NorthQThing thing) {
        String name = thing.getName();
        if (thing instanceof Thermostat) {
            name = NorthQBindingBindingConstants.THERMOSTAT_NAME;
        }
        String roomId;
        if (roomMap.isEmpty()) {
            roomMap = bridgeHandler.getAllRooms().stream().collect(Collectors.toMap(Room::getId, Function.identity()));
        }
        if (roomMap.containsKey(thing.getRoom())) {
            roomId = String.format("%d%s%s", thing.getRoom(), NorthQBindingBindingConstants.ROOM_ID_SEPERATOR,
                    roomMap.get(thing.getRoom()).getName());
        } else {
            roomId = String.valueOf(thing.getRoom());
        }
        ThingUID thingUID = getThingUID(thing);
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        Map<String, Object> properties = new HashMap<>(1);
        properties.put(NorthQBindingBindingConstants.UNIQUE_ID, thing.getUniqueId());
        properties.put(NorthQBindingBindingConstants.ROOM_ID, roomId);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withProperties(properties).withBridge(bridgeUID).withLabel(name).build();
        thingDiscovered(discoveryResult);
    }

    private ThingUID getThingUID(NorthQThing thing) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        return new ThingUID(thingTypeUID, bridgeUID, thing.getUniqueId());
    }

    @Override
    public void onThingStateChanged(NorthQThing thing) {
        // Not used
    }

    @Override
    public void onThingRemoved(NorthQThing thing) {
        ThingUID thingUID = getThingUID(thing);

        if (thingUID != null) {
            thingRemoved(thingUID);
        }
    }

    @Override
    public void onThingAdded(NorthQThing thing) {
        if (thingActive(thing)) {
            addNorthQThing(thing);
        }
    }
}
