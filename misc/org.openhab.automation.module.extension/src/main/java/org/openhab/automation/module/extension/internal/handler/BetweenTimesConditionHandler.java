/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.automation.module.extension.internal.handler;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.openhab.automation.module.extension.internal.type.BetweenTimesConditionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Kåre Jørgensen - Initial Contribution
 *
 */
public class BetweenTimesConditionHandler extends BaseModuleHandler<Condition> implements ConditionHandler {
    private final Logger logger = LoggerFactory.getLogger(BetweenTimesConditionHandler.class);

    public BetweenTimesConditionHandler(Condition module) {
        super(module);
    }

    @Override
    public boolean isSatisfied(Map<String, Object> context) {
        String firstTimeString = (String) module.getConfiguration().get(BetweenTimesConditionType.CONFIG_FIRST_TIME);
        String secondTimeString = (String) module.getConfiguration().get(BetweenTimesConditionType.CONFIG_SECOND_TIME);
        String reg = "^([0-1][0-9]|2[0-3]):([0-5][0-9])$";
        if (firstTimeString.matches(reg) && secondTimeString.matches(reg)) {
            boolean valid = false;

            String[] firstTimeSplit = firstTimeString.split(":");
            String[] secondTimeSplit = secondTimeString.split(":");

            Calendar firstCalendar = Calendar.getInstance();
            firstCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(firstTimeSplit[0]));
            firstCalendar.set(Calendar.MINUTE, Integer.parseInt(firstTimeSplit[1]));

            Calendar secondCalendar = Calendar.getInstance();
            secondCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(secondTimeSplit[0]));
            secondCalendar.set(Calendar.MINUTE, Integer.parseInt(secondTimeSplit[1]));

            Calendar currentCalendar = Calendar.getInstance();

            if (secondTimeString.compareTo(firstTimeString) < 0) {
                secondCalendar.add(Calendar.DATE, 1);
            }

            Date actualTime = currentCalendar.getTime();
            if ((actualTime.after(firstCalendar.getTime()) || actualTime.compareTo(firstCalendar.getTime()) == 0)
                    && actualTime.before(secondCalendar.getTime())) {
                valid = true;
            }
            return valid;
        } else {
            logger.warn("String could not be parsed to date. Expecting HH:mm form. firstTime: {}, secondTime: {}",
                    firstTimeString, secondTimeString);
            return false;
        }
    }
}
