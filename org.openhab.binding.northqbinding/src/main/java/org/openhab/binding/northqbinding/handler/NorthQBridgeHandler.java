/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.northqbinding.handler;

import static org.openhab.binding.northqbinding.NorthQBindingBindingConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.northqbinding.exceptions.APIException;
import org.openhab.binding.northqbinding.exceptions.GatewayOfflineException;
import org.openhab.binding.northqbinding.exceptions.UnauthorizedException;
import org.openhab.binding.northqbinding.models.BinarySensor;
import org.openhab.binding.northqbinding.models.BinarySwitch;
import org.openhab.binding.northqbinding.models.NorthQThing;
import org.openhab.binding.northqbinding.models.Room;
import org.openhab.binding.northqbinding.models.Thermostat;
import org.openhab.binding.northqbinding.network.QStickBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;;

/**
 *
 * @author Kaare Joergensen - Initial contribution
 */
public class NorthQBridgeHandler extends ConfigStatusBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(NorthQBridgeHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);
    private static final int REFRESH = 15;
    private static final int CHANGED = 1, ADDED = 2;

    private QStickBridge qStickBridge;
    List<NorthQThing> things = new ArrayList<>();
    private Map<String, NorthQThing> thingMap = new ConcurrentHashMap<>();

    private List<BindingHandlerInterface> handlers = new ArrayList<>();
    private ScheduledFuture<?> refreshJob;
    private Runnable networkRunable = () -> {
        logger.debug("Running NorthQ refresh");
        List<String> gatewayStatuses = bridgeCallWithErrorHandling(() -> {
            return qStickBridge.getAllGatewayStatuses();
        });
        if (gatewayStatuses != null) {
            things = bridgeCallWithErrorHandling(() -> {
                return qStickBridge.getAllThings(gatewayStatuses);
            });
            logger.debug("Notifying {} handlers of {} things", handlers.size(), things.size());
            for (NorthQThing thing : things) {
                if (thingMap.containsKey(thing.getUniqueId())) {
                    notifyHandlers(thing, CHANGED);
                } else {
                    notifyHandlers(thing, ADDED);
                }
                thingMap.put(thing.getUniqueId(), thing);
            }
            // thingMap.keySet().retainAll(things.stream().map(NorthQThing::getUniqueId).collect(Collectors.toList()));
            updateStatus(ThingStatus.ONLINE);
        }
    };

    public NorthQBridgeHandler(@NonNull Bridge bridge) {
        super(bridge);
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        // Not needed
        return null;
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, Command command) {
        // Not needed
    }

    @Override
    public void dispose() {
        logger.debug("Disposing NorthQBridgeHandler");
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
        if (qStickBridge != null) {
            qStickBridge = null;
        }
        super.dispose();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Q-Stick bridge");
        if (getConfig().get(USER_NAME) != null && getConfig().get(PASSWORD) != null) {
            try {
                qStickBridge = new QStickBridge((String) getConfig().get(USER_NAME),
                        (String) getConfig().get(PASSWORD));
                logger.debug("Logged in to NorthQ");
                updateStatus(ThingStatus.ONLINE);
                startAutomaticRefresh();
            } catch (APIException | IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR);
                logger.warn("Authentication error: {}", e.getMessage());
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-ip-address");
            logger.warn("Configuration error. Lacking user or password.");
        }
    }

    private synchronized void startAutomaticRefresh() {
        if (qStickBridge != null) {
            if (refreshJob == null || refreshJob.isCancelled()) {
                refreshJob = scheduler.scheduleWithFixedDelay(networkRunable, 0, REFRESH, TimeUnit.SECONDS);
            }
        }
    }

    public void removeThing(NorthQThing thing) {
        if (thing != null) {
            thingMap.remove(thing);
        }
    }

    public NorthQThing getThingByUniqueId(String id) {
        if (id == null || id.isEmpty() || thingMap == null) {
            return null;
        }
        return thingMap.get(id);
    }

    public List<Thermostat> getThermostatsByRoomId(int roomId) {
        List<Thermostat> thermostats = new ArrayList<>();
        for (NorthQThing thing : things) {
            if (thing instanceof Thermostat && thing.getRoom() == roomId) {
                thermostats.add((Thermostat) thing);
            }
        }
        return thermostats;
    }

    public List<Room> getAllRooms() {
        List<Room> rooms = bridgeCallWithErrorHandling(() -> {
            return qStickBridge.getAllRooms();
        });
        return rooms;
    }

    public Room getRoomById(String roomId) {
        int roomIdInt = Integer.valueOf(roomId);
        List<Room> rooms = getAllRooms();
        for (Room room : rooms) {
            if (room.getId() == roomIdInt) {
                return room;
            }
        }
        return null;
    }

    public List<NorthQThing> getAllNorthQThings() {
        List<String> gatewayStatuses = bridgeCallWithErrorHandling(() -> {
            return qStickBridge.getAllGatewayStatuses();
        });
        if (gatewayStatuses != null) {
            bridgeCallWithErrorHandling(() -> {
                updateLocalCache(qStickBridge.getAllThings(gatewayStatuses));
                return null;
            });
        }
        return things;
    }

    private void updateLocalCache(List<NorthQThing> newThings) {
        things = newThings;
        things.stream().forEach(t -> thingMap.put(t.getUniqueId(), t));
    }

    public void updateHousesAndGateways() {
        bridgeCallWithErrorHandling(() -> {
            qStickBridge.updateHousesAndGateways();
            return null;
        });
    }

    @SuppressWarnings("null")
    public <T> T bridgeCallWithErrorHandling(Callable<T> methodCall) {
        if (qStickBridge != null) {
            try {
                try {
                    return methodCall.call();
                } catch (GatewayOfflineException e) {
                    logger.debug("{}", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                } catch (UnauthorizedException e) {
                    logger.debug("{}", e.getMessage());
                    onAuthenticationError();
                } catch (APIException | IOException e) {
                    logger.debug("{}", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            } catch (Exception e) {
                logger.debug("Bridge failed {} call", methodCall.toString(), e);
            }
        }
        return null;
    }

    private boolean onAuthenticationError() {
        if (getConfig().get(USER_NAME) != null && getConfig().get(PASSWORD) != null) {
            try {
                qStickBridge = new QStickBridge((String) getConfig().get(USER_NAME),
                        (String) getConfig().get(PASSWORD));
                return true;
            } catch (APIException | IOException e) {
                logger.warn("User not authenticated");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            }
        }

        return false;
    }

    public void changeSwitchState(BinarySwitch binarySwitch) throws IOException, APIException {
        qStickBridge.changeSwitchState(binarySwitch);
    }

    public void armSensor(BinarySensor binarySensor) throws IOException, APIException {
        qStickBridge.armSensor(binarySensor);
    }

    public void disArmSensor(BinarySensor binarySensor) throws IOException, APIException {
        qStickBridge.disArmSensor(binarySensor);
    }

    public void setRoomTemperature(int roomId, String gatewaySerial, double newTemperature)
            throws IOException, APIException {
        qStickBridge.setRoomTemperature(roomId, gatewaySerial, newTemperature);
    }

    private void notifyHandlers(NorthQThing thing, int type) {
        if (type == CHANGED) {
            handlers.stream().forEach(h -> h.onThingStateChanged(thing));
        } else {
            handlers.stream().forEach(h -> h.onThingAdded(thing));
        }

    }

    public boolean addHandler(BindingHandlerInterface handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Null handler not allowed");
        }
        boolean result = handlers.add(handler);
        startAutomaticRefresh();
        if (result) {
            for (NorthQThing thing : things) {
                handler.onThingStateChanged(thing);
            }
        }
        return result;
    }

    public boolean removeHandler(BindingHandlerInterface handler) {
        return handlers.remove(handler);
    }

}
