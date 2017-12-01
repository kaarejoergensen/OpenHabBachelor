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
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.events.RuleStatusInfoEvent;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemProvider;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.State;
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
public class AboveBelowTriggerTest extends JavaOSGiTest {
    private final VolatileStorageService volatileStorageService = new VolatileStorageService();

    private RuleRegistry ruleRegistry;
    private ItemProvider itemProvider;
    private ItemRegistry itemRegistry;

    @Before
    public void setUp() {
        registerService(volatileStorageService);
        ruleRegistry = getService(RuleRegistry.class, RuleRegistry.class);
        assertThat(ruleRegistry, is(notNullValue()));
        itemRegistry = getService(ItemRegistry.class, ItemRegistry.class);
        assertThat(itemRegistry, is(notNullValue()));
        itemProvider = new ItemProvider() {

            @Override
            public void removeProviderChangeListener(ProviderChangeListener<@NonNull Item> listener) {
            }

            @Override
            public Collection<@NonNull Item> getAll() {
                return Arrays.asList(new Item[] { new NumberItem("numberItem1"), new SwitchItem("switchItem1") {
                    {
                        setState(OnOffType.OFF);
                    }
                }, new SwitchItem("switchItem2") });
            }

            @Override
            public void addProviderChangeListener(ProviderChangeListener<@NonNull Item> listener) {
            }
        };
        registerService(itemProvider);
    }

    @After
    public void tearDown() {
        unregisterService(volatileStorageService);
    }

    @Test
    public void ruleTriggeredByAboveNumberTest() {
        testAboveBelowTrigger("numberItem1", ">", "20", DecimalType.valueOf("18"), DecimalType.valueOf("21"), true);
    }

    @Test
    public void ruleTriggeredByBelowNumberTest() {
        testAboveBelowTrigger("numberItem1", "<", "20", DecimalType.valueOf("21"), DecimalType.valueOf("18"), true);
    }

    @Test
    public void ruleTriggeredByEqualsNumberTest() {
        testAboveBelowTrigger("numberItem1", "=", "20", DecimalType.valueOf("21"), DecimalType.valueOf("20"), true);
        testAboveBelowTrigger("numberItem1", "=", "20", DecimalType.valueOf("19"), DecimalType.valueOf("20"), true);
    }

    @Test
    public void ruleTriggeredByEqualsSwitchTest() {
        testAboveBelowTrigger("switchItem2", "=", "ON", OnOffType.OFF, OnOffType.ON, true);
    }

    @Test
    public void ruleNotTriggeredWhenAlreadyBelowTest() {
        testAboveBelowTrigger("numberItem1", "<", "20", DecimalType.valueOf("19"), DecimalType.valueOf("18"), false);
    }

    @Test
    public void ruleNotTriggeredWhenAlreadyAboveTest() {
        testAboveBelowTrigger("numberItem1", ">", "20", DecimalType.valueOf("21"), DecimalType.valueOf("22"), false);
    }

    @Test
    public void ruleNotTriggeredWhenAlreadyEqualsNumberTest() {
        testAboveBelowTrigger("numberItem1", "=", "20", DecimalType.valueOf("20"), DecimalType.valueOf("20"), false);
    }

    @Test
    public void ruleNotTriggeredWhenAlreadyEqualsSwitchTest() {
        testAboveBelowTrigger("switchItem2", "=", "ON", OnOffType.ON, OnOffType.ON, false);
    }

    private void testAboveBelowTrigger(String itemName, String operator, String checkState, State oldState,
            State newState, boolean expectTriggered) {
        final Configuration triggerConfig = new Configuration(Stream
                .of(new SimpleEntry<>("itemName", itemName), new SimpleEntry<>("operator", operator),
                        new SimpleEntry<>("state", checkState))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));

        final Configuration actionConfig = new Configuration(
                Stream.of(new SimpleEntry<>("itemName", "switchItem1"), new SimpleEntry<>("command", "ON"))
                        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
        final Rule rule = new Rule("myRule" + new Random().nextInt());
        rule.setTriggers(Arrays.asList(new Trigger[] {
                new Trigger("ItemCommandAboveBelowTrigger1", "ItemCommandAboveBelowTrigger", triggerConfig) }));
        rule.setActions(Arrays.asList(
                new Action[] { new Action("ItemPostCommandAction1", "core.ItemCommandAction", actionConfig, null) }));
        rule.setName("TestRuleTriggeredByAbove");

        ruleRegistry.add(rule);

        waitForAssert(() -> {
            assertThat(ruleRegistry.getStatusInfo(rule.getUID()).getStatus(), is(RuleStatus.IDLE));
        });

        Queue<Event> events = new LinkedList<>();

        registerService(new EventSubscriber() {
            @Override
            public void receive(Event event) {
                events.add(event);
            }

            @Override
            public Set<String> getSubscribedEventTypes() {
                return Collections.singleton(RuleStatusInfoEvent.TYPE);
            }

            @Override
            public EventFilter getEventFilter() {
                return null;
            }
        });

        final EventPublisher eventPublisher = getService(EventPublisher.class);
        assertThat(eventPublisher, is(notNullValue()));
        eventPublisher.post(ItemEventFactory.createStateEvent(itemName, oldState));
        eventPublisher.post(ItemEventFactory.createStateEvent(itemName, newState));

        if (expectTriggered) {
            waitForAssert(() -> {
                assertThat(events.isEmpty(), is(false));
                RuleStatusInfoEvent event = (RuleStatusInfoEvent) events.remove();
                assertThat(event.getStatusInfo().getStatus(), is(RuleStatus.RUNNING));
            });
        } else {
            waitForAssert(() -> {
                assertThat(events.isEmpty(), is(true));
            });
        }

        waitForAssert(() -> {
            Item item = itemRegistry.get("switchItem1");
            assertThat(item, is(notNullValue()));
            assertThat(item.getState(), is(expectTriggered ? OnOffType.ON : OnOffType.OFF));
        });
    }
}
