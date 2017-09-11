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

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "binary-switch");

    // List of all Channel ids
    public static final String CHANNEL_1 = "channel1";

}
