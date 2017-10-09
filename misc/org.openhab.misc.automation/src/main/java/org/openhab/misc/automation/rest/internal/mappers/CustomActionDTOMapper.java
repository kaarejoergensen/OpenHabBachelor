/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.misc.automation.rest.internal.mappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.config.core.Configuration;
import org.openhab.misc.automation.rest.internal.models.CustomActionDTO;

public class CustomActionDTOMapper {

    public static List<Action> mapDTO(List<CustomActionDTO> list) {
        if (list == null) {
            return null;
        }
        List<Action> actions = new ArrayList<>(list.size());
        for (CustomActionDTO customActionDTO : list) {
            actions.add(map(customActionDTO));
        }
        return actions;
    }

    private static Action map(CustomActionDTO customActionDTO) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("command", customActionDTO.getCommand());
        properties.put("itemName", customActionDTO.getItemName());
        IdContainerSingleton idContainerSingleton = IdContainerSingleton.getInstance();
        Action action = new Action(idContainerSingleton.getModuleId(), "core.ItemCommandAction",
                new Configuration(properties), null);
        action.setLabel("label");
        action.setDescription("description");
        // TODO: Finish method
        return action;
    }
}
