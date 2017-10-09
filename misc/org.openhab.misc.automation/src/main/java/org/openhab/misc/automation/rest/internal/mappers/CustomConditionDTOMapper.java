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
import java.util.stream.Collectors;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.Configuration;
import org.openhab.misc.automation.rest.internal.models.CustomConditionDTO;

public class CustomConditionDTOMapper {

    public static List<Condition> mapDTO(List<CustomConditionDTO> list) {
        if (list == null) {
            return null;
        }
        List<Condition> conditions = new ArrayList<>(list.size());
        for (CustomConditionDTO customConditionDTO : list) {
            conditions.add(mapCondition(customConditionDTO));
        }
        return conditions;
    }

    private static Condition mapCondition(CustomConditionDTO customConditionDTO) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("itemName", customConditionDTO.getItemName());
        properties.put("operator", customConditionDTO.getOperator());
        properties.put("state", customConditionDTO.getState());
        IdContainerSingleton idContainerSingleton = IdContainerSingleton.getInstance();
        Condition condition = new Condition(idContainerSingleton.getModuleId(), "core.ItemStateCondition",
                new Configuration(properties), null);
        condition.setLabel("label");
        condition.setDescription("description");
        // TODO: Finish method
        return condition;
    }

    public static List<Trigger> inferTriggers(List<CustomConditionDTO> list) {
        if (list == null) {
            return null;
        }
        List<Trigger> triggers = new ArrayList<>(list.size());
        for (CustomConditionDTO customConditionDTO : list) {
            triggers.add(mapTrigger(customConditionDTO));
        }
        return triggers.stream().distinct().collect(Collectors.toList());
    }

    private static Trigger mapTrigger(CustomConditionDTO customConditionDTO) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("itemName", customConditionDTO.getItemName());
        IdContainerSingleton idContainerSingleton = IdContainerSingleton.getInstance();
        Trigger trigger = new Trigger(idContainerSingleton.getModuleId(), inferType(customConditionDTO),
                new Configuration(properties));
        trigger.setLabel("label");
        trigger.setDescription("description");
        // TODO: Finish method
        return trigger;
    }

    private static String inferType(CustomConditionDTO customConditionDTO) {
        // TODO: Create method
        switch ("core.ItemStateCondition") {
            case "core.ItemStateCondition":
                return "core.ItemStateUpdateTrigger";
            default:
                return "core.ItemStateUpdateTrigger";
        }
    }
}
