/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.northq;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link NorthQBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kaare Joergensen - Initial contribution
 */
public class NorthQBindingConstants {

    private static final String BINDING_ID = "northq";

    // Bride type
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // List of all Thing Type UIDs
    public static final ThingTypeUID BINARY_SWITCH = new ThingTypeUID(BINDING_ID, "binary-switch");
    public static final ThingTypeUID BINARY_SENSOR = new ThingTypeUID(BINDING_ID, "binary-sensor");
    public static final ThingTypeUID THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");

    // List of all Channel ids
    public static final String BINARY_SWITCH_SWITCH_CHANNEL = "binaryswitch_switch_channel";
    public static final String BINARY_SWITCH_WATTAGE_CHANNEL = "binaryswitch_wattage_channel";

    public static final String BINARY_SENSOR_ARM_CHANNEL = "binarysensor_arm_channel";
    public static final String BINARY_SENSOR_TEMP_CHANNEL = "binarysensor_temperature_channel";
    public static final String BINARY_SENSOR_LUMINANCE_CHANNEL = "binarysensor_luminance_channel";
    public static final String BINARY_SENSOR_HUMIDITY_CHANNEL = "binarysensor_humidity_channel";
    public static final String BINARY_SENSOR_TRIGGERED_CHANNEL = "binarysensor_triggered_channel";

    public static final String THERMOSTAT_TEMP_CHANNEL = "thermostat_temperature_channel";

    // List of all config string
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String UNIQUE_ID = "uniqueId";
    public static final String ROOM_ID = "roomId";

    // Network constants

    // Other constants
    public static final String ROOM_ID_SEPERATOR = " - ";
}
