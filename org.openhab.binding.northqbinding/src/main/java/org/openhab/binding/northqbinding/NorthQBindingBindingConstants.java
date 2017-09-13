/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.northqbinding;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link NorthQBindingBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kaare Joergensen - Initial contribution
 */
public class NorthQBindingBindingConstants {

    private static final String BINDING_ID = "northqbinding";

    // Bride type
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // List of all Thing Type UIDs
    public static final ThingTypeUID BINARY_SWITCH = new ThingTypeUID(BINDING_ID, "binary-switch");

    // List of all Channel ids
    public static final String BINARY_SWITCH_SWITCH_CHANNEL = "binaryswitch_switch_channel";
    public static final String BINARY_SWITCH_WATTAGE_CHANNEL = "binaryswitch_wattage_channel";

    // List of all config string
    public static final String USER_NAME = "username";
    public static final String PASSWORD = "password";
    public static final String NODE_ID = "nodeId";
}
