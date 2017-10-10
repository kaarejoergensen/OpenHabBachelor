/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.misc.automation.rest.internal.mappers;

import java.util.Collections;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.config.core.Configuration;
import org.openhab.misc.automation.rest.internal.models.CustomRuleDTO;

public class CustomRuleDTOMapper {

    public static Rule map(CustomRuleDTO customRuleDTO) {
        IdContainerSingleton containerSingleton = IdContainerSingleton.getInstance();
        containerSingleton.reset();
        Rule rule = new Rule(customRuleDTO.getUid(),
                CustomConditionDTOMapper.inferTriggers(customRuleDTO.getConditions()),
                CustomConditionDTOMapper.mapDTO(customRuleDTO.getConditions()),
                CustomActionDTOMapper.mapDTO(customRuleDTO.getActions()), Collections.emptyList(), new Configuration(),
                null, null);
        rule.setName(customRuleDTO.getName());
        rule.setDescription(customRuleDTO.getDescription());
        // TODO: Finish method
        return rule;
    }

    public static CustomRuleDTO map(Rule rule) {
        return new CustomRuleDTO(rule.getUID(), rule.getName(), rule.getDescription(),
                CustomConditionDTOMapper.mapCondition(rule.getConditions()),
                CustomActionDTOMapper.mapAction(rule.getActions()));
    }
}
