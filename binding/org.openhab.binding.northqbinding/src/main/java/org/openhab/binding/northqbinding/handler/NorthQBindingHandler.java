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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.northqbinding.NorthQBindingBindingConstants;
import org.openhab.binding.northqbinding.models.BinarySensor;
import org.openhab.binding.northqbinding.models.BinarySensor.Sensor;
import org.openhab.binding.northqbinding.models.BinarySwitch;
import org.openhab.binding.northqbinding.models.NorthQThing;
import org.openhab.binding.northqbinding.models.Room;
import org.openhab.binding.northqbinding.models.Thermostat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link NorthQBindingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kaare Joergensen - Initial contribution
 */
public class NorthQBindingHandler extends BaseThingHandler implements BindingHandlerInterface {
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(BINARY_SWITCH, BINARY_SENSOR,
            THERMOSTAT);

    private final Logger logger = LoggerFactory.getLogger(NorthQBindingHandler.class);

    private NorthQBridgeHandler bridgeHandler;
    private String uniqueId;
    private String roomId;

    private double lastThermostatTemperature = 0;
    private long lastThermostatUpdateTime = 0L;

    public NorthQBindingHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        NorthQBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            logger.debug("No bridge found. Cannot handle command without bridge.");
            return;
        }
        NorthQThing thing = bridgeHandler.getThingByUniqueId(uniqueId);

        if (thing == null) {
            logger.debug("No thing object found. Cannot handle command without object.");
            if (getBridge().getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
            return;
        }
        if (command instanceof RefreshType) {
            logger.debug("Command is refreshtype, refresh the thing.");
            onThingStateChanged(thing);
            return;
        }
        switch (channelUID.getId()) {
            case BINARY_SWITCH_SWITCH_CHANNEL:
                if (command instanceof OnOffType) {
                    bridgeHandler.changeSwitchState((BinarySwitch) thing);
                    updateState(channelUID, ((BinarySwitch) thing).isTurnedOn() ? OnOffType.ON : OnOffType.OFF);
                }
                break;
            case BINARY_SENSOR_ARM_CHANNEL:
                if (command instanceof OnOffType) {
                    BinarySensor binarySensor = (BinarySensor) thing;
                    if (binarySensor.isArmed()) {
                        bridgeHandler.disArmSensor(binarySensor);
                        updateState(channelUID, OnOffType.OFF);
                        updateState(new ChannelUID(getThing().getUID(), BINARY_SENSOR_TRIGGERED_CHANNEL),
                                OnOffType.OFF);
                    } else {
                        bridgeHandler.armSensor(binarySensor);
                        updateState(new ChannelUID(getThing().getUID(), BINARY_SENSOR_TRIGGERED_CHANNEL),
                                binarySensor.isMotionDetected() ? OnOffType.ON : OnOffType.OFF);
                        updateState(channelUID, OnOffType.ON);
                    }
                }
                break;
            case THERMOSTAT_TEMP_CHANNEL:
                if (command instanceof DecimalType) {
                    if (((DecimalType) command).doubleValue() >= 4 && ((DecimalType) command).doubleValue() <= 28) {
                        bridgeHandler.setRoomTemperature(thing.getRoom(), thing.getGateway(),
                                ((DecimalType) command).doubleValue());
                        lastThermostatTemperature = ((DecimalType) command).doubleValue();
                        lastThermostatUpdateTime = System.currentTimeMillis();
                        updateState(new ChannelUID(getThing().getUID(), THERMOSTAT_TEMP_CHANNEL),
                                new DecimalType(((DecimalType) command).doubleValue()));
                    }
                }

                break;
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing NorthQBindingHandler");
        initializeThing(getBridge() == null ? null : getBridge().getStatus());
    }

    private void initializeThing(ThingStatus bridgeStatus) {
        logger.debug("Initialize thing {} bridge status {}", getThing().getUID(), bridgeStatus);
        String configUnique_id = (String) getConfig().get(UNIQUE_ID);
        String roomId = (String) getConfig().get(ROOM_ID);
        if (configUnique_id != null) {
            uniqueId = configUnique_id;
            updateLocation(roomId, bridgeStatus);
            if (getBridgeHandler() != null) {
                if (bridgeStatus == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    private void updateLocation(String roomId, ThingStatus bridgeStatus) {
        if (roomId != null && !roomId.isEmpty()) {
            if (roomId.contains(ROOM_ID_SEPERATOR)) {
                String[] roomParts = roomId.split(ROOM_ID_SEPERATOR);
                this.roomId = roomParts[0];
                String roomName = roomParts[1];
                getThing().setLocation(roomName);
            } else {
                this.roomId = roomId;
                if (getBridgeHandler() != null && bridgeStatus == ThingStatus.ONLINE) {
                    Room room = bridgeHandler.getRoomById(roomId);
                    if (room != null) {
                        getThing().setLocation(room.getName());
                    }
                }
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing: Unregistering handler from bridgeHandler");
        if (uniqueId != null) {
            NorthQBridgeHandler bridgeHandler = getBridgeHandler();
            if (bridgeHandler != null) {
                bridgeHandler.removeThing(bridgeHandler.getThingByUniqueId(uniqueId));
                bridgeHandler.removeHandler(this);
                this.bridgeHandler = null;
            }
            uniqueId = null;
        }
    }

    private synchronized NorthQBridgeHandler getBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof NorthQBridgeHandler) {
                this.bridgeHandler = (NorthQBridgeHandler) handler;
                this.bridgeHandler.addHandler(this);
            } else {
                return null;
            }
        }
        return this.bridgeHandler;
    }

    @Override
    public void onThingStateChanged(NorthQThing thing) {
        if (thing == null || (!thingActive(thing) && !(thing instanceof Thermostat))) {
            if (!getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                updateStatus(ThingStatus.OFFLINE);
            }
            return;
        }
        if (thing instanceof Thermostat && getThing().getThingTypeUID().equals(NorthQBindingBindingConstants.THERMOSTAT)
                && String.valueOf(thing.getRoom()).equals(roomId)) {
            getBridgeHandler();
            if (bridgeHandler != null) {
                List<Thermostat> thermostats = bridgeHandler.getThermostatsByRoomId(thing.getRoom()).stream()
                        .filter(t -> thingActive(t)).collect(Collectors.toList());
                if (!thermostats.isEmpty()) {
                    Thermostat thermostat = thermostats.get(0);
                    if (lastThermostatTemperature == thermostat.getTemperature()) {
                        lastThermostatTemperature = -1;
                    }
                    if (!updateSentInLastTenMinutes() || lastThermostatTemperature == -1) {
                        updateState(new ChannelUID(getThing().getUID(), THERMOSTAT_TEMP_CHANNEL),
                                new DecimalType(thermostat.getTemperature()));
                    }
                    if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                } else {
                    if (!getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                        updateStatus(ThingStatus.OFFLINE);
                    }
                }
            }
        } else if (thing.getUniqueId().equals(uniqueId)) {
            if (thing instanceof BinarySwitch) {
                BinarySwitch binarySwitch = (BinarySwitch) thing;
                updateState(new ChannelUID(getThing().getUID(), BINARY_SWITCH_SWITCH_CHANNEL),
                        binarySwitch.isTurnedOn() ? OnOffType.ON : OnOffType.OFF);
                updateState(new ChannelUID(getThing().getUID(), BINARY_SWITCH_WATTAGE_CHANNEL),
                        new DecimalType(binarySwitch.getWattage()));
                if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                    updateStatus(ThingStatus.ONLINE);
                }

            } else if (thing instanceof BinarySensor) {
                BinarySensor binarySensor = (BinarySensor) thing;
                updateState(new ChannelUID(getThing().getUID(), BINARY_SENSOR_ARM_CHANNEL),
                        binarySensor.isArmed() ? OnOffType.ON : OnOffType.OFF);
                for (Sensor s : binarySensor.getSensorList()) {
                    switch (s.getType()) {
                        case TEMPERATURE:
                            updateState(new ChannelUID(getThing().getUID(), BINARY_SENSOR_TEMP_CHANNEL),
                                    new DecimalType(s.getValue()));
                            break;
                        case lUMINANCE:
                            updateState(new ChannelUID(getThing().getUID(), BINARY_SENSOR_LUMINANCE_CHANNEL),
                                    new DecimalType(s.getValue()));
                            break;
                        case HUMIDITY:
                            updateState(new ChannelUID(getThing().getUID(), BINARY_SENSOR_HUMIDITY_CHANNEL),
                                    new DecimalType(s.getValue()));
                            break;
                    }
                }
                if (binarySensor.isArmed()) {
                    updateState(new ChannelUID(getThing().getUID(), BINARY_SENSOR_TRIGGERED_CHANNEL),
                            binarySensor.isMotionDetected() ? OnOffType.ON : OnOffType.OFF);
                } else {
                    updateState(new ChannelUID(getThing().getUID(), BINARY_SENSOR_TRIGGERED_CHANNEL), OnOffType.OFF);
                }
                if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                    updateStatus(ThingStatus.ONLINE);
                }
            }
        }
    }

    private boolean updateSentInLastTenMinutes() {
        Date lastUpdate = new Date(lastThermostatUpdateTime * 1000L);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -10);
        Date tenMinutesAgo = calendar.getTime();
        return tenMinutesAgo.before(lastUpdate);
    }

    private boolean thingActive(NorthQThing thing) {
        Date lastRead = new Date(thing.getRead() * 1000L);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -8);
        Date eightHoursAgo = calendar.getTime();
        return eightHoursAgo.before(lastRead);
    }

    @SuppressWarnings("null")
    @Override
    public void onThingRemoved(NorthQThing thing) {
        if (thing != null && thing.getUniqueId().equals(uniqueId)) {
            if (getThing() != null && getThing().getUID() != null) {
                thingRegistry.remove(getThing().getUID());
            }
        }
    }

    @Override
    public void onThingAdded(NorthQThing thing) {
        if (thing.getUniqueId().equals(uniqueId)) {
            updateStatus(ThingStatus.ONLINE);
            onThingStateChanged(thing);
        }
    }
}
