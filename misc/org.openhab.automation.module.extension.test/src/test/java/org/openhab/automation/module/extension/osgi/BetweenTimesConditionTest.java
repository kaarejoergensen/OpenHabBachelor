/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.automation.module.extension.osgi;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.Calendar;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openhab.automation.module.extension.internal.handler.BetweenTimesConditionHandler;

/**
 *
 * @author Kåre Jørgensen - Initial Contribution
 *
 */
public class BetweenTimesConditionTest extends JavaOSGiTest {
    private final VolatileStorageService volatileStorageService = new VolatileStorageService();

    @Before
    public void setUp() {
        registerService(volatileStorageService);
    }

    @After
    public void tearDown() {
        unregisterService(volatileStorageService);
    }

    @Test
    public void conditionSatisfiedTest() {
        testBetweenTimesCondition(-1, 1, true);
    }

    @Test
    public void conditionTimeEarlierTest() {
        testBetweenTimesCondition(-5, -4, false);
    }

    @Test
    public void conditionTimeLaterTest() {
        testBetweenTimesCondition(4, 5, false);
    }

    private void testBetweenTimesCondition(int fromTime, int toTime, boolean expectSatisfied) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");

        Calendar fromTimeCal = Calendar.getInstance();
        Calendar toTimeCal = Calendar.getInstance();
        fromTimeCal.add(Calendar.HOUR, fromTime);
        toTimeCal.add(Calendar.HOUR, toTime);
        Configuration conditionConfig = new Configuration(Stream
                .of(new SimpleEntry<>("firstTime", format.format(fromTimeCal.getTime())),
                        new SimpleEntry<>("secondTime", format.format(toTimeCal.getTime())))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
        Condition condition = new Condition("id", "BetweenTimesCondition", conditionConfig, new HashMap<>());
        BetweenTimesConditionHandler handler = new BetweenTimesConditionHandler(condition);
        assertThat(handler.isSatisfied(new HashMap<>()), is(expectSatisfied));
    }
}
