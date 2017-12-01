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
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder;

/**
 *
 * @author Kåre Jørgensen - Initial Contribution
 *
 */
public class BetweenTimesConditionType extends ConditionType {
    public static String UID = "BetweenTimesCondition";

    public static final String CONFIG_FIRST_TIME = "firstTime";
    public static final String CONFIG_SECOND_TIME = "secondTime";

    public static ConditionType initialize() {
        final ConfigDescriptionParameter firstTime = ConfigDescriptionParameterBuilder
                .create(CONFIG_FIRST_TIME, Type.TEXT).withRequired(true).withMultiple(false).withLabel("First Time")
                .withDescription("After this time").build();
        final ConfigDescriptionParameter secondTime = ConfigDescriptionParameterBuilder
                .create(CONFIG_SECOND_TIME, Type.TEXT).withRequired(true).withMultiple(false).withLabel("Second Time")
                .withDescription("Before this time").build();

        final List<ConfigDescriptionParameter> config = new ArrayList<ConfigDescriptionParameter>();
        config.add(firstTime);
        config.add(secondTime);

        return new BetweenTimesConditionType(config);
    }

    public BetweenTimesConditionType(List<ConfigDescriptionParameter> config) {
        super(UID, config, "Between two points in time condition",
                "checks if the current time is between two points in time.", null, Visibility.VISIBLE,
                new ArrayList<>());
    }
}
