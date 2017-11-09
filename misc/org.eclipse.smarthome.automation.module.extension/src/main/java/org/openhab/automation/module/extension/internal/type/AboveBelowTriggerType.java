/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.automation.module.extension.internal.type;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder;

/**
 *
 * @author Kåre Jørgensen - Initial Contribution
 *
 */
public class AboveBelowTriggerType extends TriggerType {
    public static String UID = "ItemCommandAboveBelowTrigger";

    public static final String CONFIG_ITEM_NAME = "itemName";
    public static final String CONFIG_OPERATOR = "operator";
    public static final String CONFIG_STATE = "state";
    public static final String CONFIG_EVENT = "event";

    public static TriggerType initialize() {
        final ConfigDescriptionParameter itemName = ConfigDescriptionParameterBuilder
                .create(CONFIG_ITEM_NAME, Type.TEXT).withContext("item").withRequired(true).withMultiple(false)
                .withLabel("Item").withDescription("the name of the item").build();
        final ConfigDescriptionParameter operator = ConfigDescriptionParameterBuilder.create(CONFIG_OPERATOR, Type.TEXT)
                .withRequired(true).withMultiple(false).withLabel("Operator")
                .withDescription("the compare operator (one of =,<,>,!=,>=,<=)").build();
        final ConfigDescriptionParameter state = ConfigDescriptionParameterBuilder.create(CONFIG_STATE, Type.TEXT)
                .withRequired(true).withMultiple(false).withLabel("State")
                .withDescription("the state to be compared with").build();

        final List<ConfigDescriptionParameter> config = new ArrayList<ConfigDescriptionParameter>();
        config.add(itemName);
        config.add(operator);
        config.add(state);

        List<Output> output = new ArrayList<Output>();

        Output stateOutput = new Output(CONFIG_STATE, "state", "State", "the item state", null, null, null);
        Output event = new Output(CONFIG_EVENT, "org.eclipse.smarthome.core.events.Event", "Event",
                "The events which was sent", null, "event", null);
        output.add(stateOutput);
        output.add(event);

        return new AboveBelowTriggerType(config, output);
    }

    public AboveBelowTriggerType(List<ConfigDescriptionParameter> config, List<Output> output) {
        super(UID, config, "an item raises above/drops below a value",
                "This triggers the rule if the item raises above/drops below a certain value.", null,
                Visibility.VISIBLE, output);
    }
}
