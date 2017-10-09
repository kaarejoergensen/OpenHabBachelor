/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.misc.automation.rest.internal.mappers;

public class IdContainerSingleton {
    private static IdContainerSingleton containerSingleton;

    private int maxId;

    private IdContainerSingleton() {
        this.maxId = 0;
    }

    public static IdContainerSingleton getInstance() {
        if (containerSingleton == null) {
            containerSingleton = new IdContainerSingleton();
        }
        return containerSingleton;
    }

    public String getModuleId() {
        return String.valueOf(++this.maxId);
    }

    public void reset() {
        containerSingleton = new IdContainerSingleton();
    }
}
