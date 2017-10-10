/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.automation.internal;

import org.openhab.ui.dashboard.DashboardTile;

/**
 *
 *
 * @author Kaare Joergensen - Initial contribution
 */
public class AutomationUIDashboardTile implements DashboardTile {

    @Override
    public String getImageUrl() {
        // TODO Get image
        return "";
    }

    @Override
    public String getName() {
        return "Automation UI";
    }

    @Override
    public String getOverlay() {
        return null;
    }

    @Override
    public String getUrl() {
        return "../automation/index.html";
    }

}
