/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.automation.module.extension.osgi;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemProvider;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Kåre Jørgensen - Initial Contribution
 *
 */
public class RuntimeRuleTest extends JavaOSGiTest {
    private final VolatileStorageService volatileStorageService = new VolatileStorageService();

    private RuleRegistry ruleRegistry;

    @Before
    public void setUp() {
        registerService(volatileStorageService);
        ruleRegistry = getService(RuleRegistry.class, RuleRegistry.class);
        assertThat(ruleRegistry, is(notNullValue()));
        registerService(new ItemProvider() {

            @Override
            public void removeProviderChangeListener(ProviderChangeListener<@NonNull Item> listener) {
            }

            @Override
            public Collection<@NonNull Item> getAll() {
                return Arrays.asList(new Item[] { new NumberItem("numberItem1") });
            }

            @Override
            public void addProviderChangeListener(ProviderChangeListener<@NonNull Item> listener) {
            }
        });
    }

    @After
    public void tearDown() {
        unregisterService(volatileStorageService);
    }

    @Test
    public void ruleTriggeredByAboveTest() {
        final Configuration triggerConfig = new Configuration(Stream
                .of(new SimpleEntry<>("itemName", "numberItem1"), new SimpleEntry<>("operator", ">"),
                        new SimpleEntry<>("state", "20"))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));

        final Configuration actionConfig = new Configuration(
                Stream.of(new SimpleEntry<>("itemName", "myLampItem2"), new SimpleEntry<>("command", "ON"))
                        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
        final Rule rule = new Rule("myRule" + new Random().nextInt());
        rule.setTriggers(Arrays.asList(new Trigger[] {
                new Trigger("ItemCommandAboveBelowTrigger1", "core.ItemCommandAboveBelowTrigger", triggerConfig) }));
        rule.setActions(Arrays.asList(
                new Action[] { new Action("ItemPostCommandAction1", "core.ItemCommandAction", actionConfig, null) }));
        rule.setName("TestRuleTriggeredByAbove");

        ruleRegistry.add(rule);

        waitForAssert(() -> {
            System.out.println(ruleRegistry.getStatusInfo(rule.getUID()));
            System.out.println(ruleRegistry.getStatusInfo(rule.getUID()).getStatusDetail());
            assertThat(ruleRegistry.getStatusInfo(rule.getUID()).getStatus(), is(RuleStatus.IDLE));

        });
    }
}
