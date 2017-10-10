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
import java.util.List;

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
        String id = customActionDTO.getId();
        if (id == null) {
            IdContainerSingleton idContainerSingleton = IdContainerSingleton.getInstance();
            id = idContainerSingleton.getModuleId();
        }
        Action action = new Action(id, "core.ItemCommandAction", new Configuration(customActionDTO.getProperties()),
                null);
        action.setLabel("label");
        action.setDescription("description");
        // TODO: Finish method
        return action;
    }

    public static List<CustomActionDTO> mapAction(List<Action> actions) {
        if (actions == null) {
            return null;
        }
        List<CustomActionDTO> customActionDTOs = new ArrayList<>(actions.size());
        for (Action action : actions) {
            customActionDTOs.add(map(action));
        }
        return customActionDTOs;
    }

    private static CustomActionDTO map(Action action) {
        CustomActionDTO customActionDTO = new CustomActionDTO();

        customActionDTO.setId(action.getId());
        customActionDTO.setProperties(action.getConfiguration().getProperties());
        customActionDTO.setType(customActionDTO.getType());

        return customActionDTO;
    }
}
