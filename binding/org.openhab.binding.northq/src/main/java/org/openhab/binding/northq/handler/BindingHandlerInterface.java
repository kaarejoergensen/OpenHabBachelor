/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.northq.handler;

import org.openhab.binding.northq.models.NorthQThing;

/**
 *
 * @author Kaare Joergensen - Initial contribution
 */
public interface BindingHandlerInterface {

    void onThingStateChanged(NorthQThing thing);

    void onThingAdded(NorthQThing thing);

    void onThingRemoved(NorthQThing thing);

}
