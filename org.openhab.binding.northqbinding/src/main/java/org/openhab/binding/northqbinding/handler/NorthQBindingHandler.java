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
import java.util.Set;

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
import org.openhab.binding.northqbinding.exceptions.APIException;
import org.openhab.binding.northqbinding.models.BinarySensor;
import org.openhab.binding.northqbinding.models.BinarySensor.Sensor;
import org.openhab.binding.northqbinding.models.BinarySwitch;
import org.openhab.binding.northqbinding.models.NorthQThing;
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
    private String node_id;

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
        bridgeHandler.getAllNorthQThings();
        BinarySwitch binarySwitch = bridgeHandler.getBinarySwitchById(node_id);
        BinarySensor binarySensor = bridgeHandler.getBinarySensorById(node_id);
        Thermostat thermostat = bridgeHandler.getThermostatById(node_id);

        if (binarySwitch == null && binarySensor == null && thermostat == null) {
            logger.debug("No BinarySwitch object found. Cannot handle command without object.");
            return;
        }
        try {
            if (command instanceof RefreshType) {
                switch (channelUID.getId()) {
                    case BINARY_SWITCH_WATTAGE_CHANNEL:
                        updateState(channelUID, new DecimalType(binarySwitch.getWattage()));
                        break;
                    case BINARY_SWITCH_SWITCH_CHANNEL:
                        if (binarySwitch.isTurnedOn()) {
                            updateState(channelUID, OnOffType.ON);
                        } else {
                            updateState(channelUID, OnOffType.OFF);
                        }
                        break;
                    case BINARY_SENSOR_ARM_CHANNEL:
                        if (binarySensor.isArmed()) {
                            updateState(channelUID, OnOffType.ON);
                        } else {
                            updateState(channelUID, OnOffType.OFF);
                        }
                        break;
                    case THERMOSTAT_TEMP_CHANNEL:
                        thermostat = bridgeHandler.getThermostatById(node_id);

                        break;

                }
            } else {
                switch (channelUID.getId()) {
                    case BINARY_SWITCH_SWITCH_CHANNEL:
                        bridgeHandler.changeSwitchState(binarySwitch);
                        if (binarySwitch.isTurnedOn()) {
                            updateState(channelUID, OnOffType.OFF);
                        } else {
                            updateState(channelUID, OnOffType.ON);
                        }
                        break;
                    case BINARY_SENSOR_ARM_CHANNEL:
                        if (binarySensor.isArmed()) {
                            bridgeHandler.disArmSensor(binarySensor);
                            updateState(channelUID, OnOffType.OFF);
                        } else {
                            bridgeHandler.armSensor(binarySensor);
                            updateState(channelUID, OnOffType.ON);
                        }
                        break;
                }

            }
        } catch (IOException | APIException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing NorthQBindingHandler");
        initializeThing(getBridge() == null ? null : getBridge().getStatus());
    }

    private void initializeThing(ThingStatus bridgeStatus) {
        logger.debug("Initialize thing " + getThing().getUID() + " bridge status " + bridgeStatus);
        String configNode_id = (String) getConfig().get(NODE_ID);
        if (configNode_id != null) {
            node_id = configNode_id;
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

    @Override
    public void dispose() {
        logger.debug("Disposing: Unregistering handler from bridgeHandler");
        if (node_id != null) {
            NorthQBridgeHandler bridgeHandler = getBridgeHandler();
            if (bridgeHandler != null) {
                bridgeHandler.removeHandler(this);
                this.bridgeHandler = null;
            }
            node_id = null;
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
        if (thing != null && String.valueOf(thing.getNode_id()).equals(node_id)) {
            if (thing instanceof BinarySwitch) {
                BinarySwitch binarySwitch = (BinarySwitch) thing;
                updateState(new ChannelUID(getThing().getUID(), BINARY_SWITCH_SWITCH_CHANNEL),
                        binarySwitch.isTurnedOn() ? OnOffType.ON : OnOffType.OFF);
                updateState(new ChannelUID(getThing().getUID(), BINARY_SWITCH_WATTAGE_CHANNEL),
                        new DecimalType(binarySwitch.getWattage()));
                updateStatus(ThingStatus.ONLINE);
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
            }
        }
    }

    @Override
    public void onThingRemoved(NorthQThing thing) {
        if (thing != null && String.valueOf(thing.getNode_id()).equals(node_id)) {
            updateStatus(ThingStatus.OFFLINE);
        }
    }
}
