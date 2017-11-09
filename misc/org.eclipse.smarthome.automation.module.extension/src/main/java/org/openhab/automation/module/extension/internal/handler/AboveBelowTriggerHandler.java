/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.automation.module.extension.internal.handler;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseTriggerModuleHandler;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.GroupItemStateChangedEvent;
import org.eclipse.smarthome.core.items.events.ItemStateChangedEvent;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.eclipse.smarthome.core.types.UnDefType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Kåre Jørgensen - Initial Contribution
 *
 */
public class AboveBelowTriggerHandler extends BaseTriggerModuleHandler implements EventSubscriber, EventFilter {
    private final Logger logger = LoggerFactory.getLogger(AboveBelowTriggerHandler.class);

    private String itemName;
    private String operator;
    private String state;
    private Set<String> types;
    private BundleContext bundleContext;

    public static final String ABOVE_BELOW_TYPE_ID = "core.ItemCommandAboveBelowTrigger";

    private static final String CFG_ITEMNAME = "itemName";
    private static final String CFG_OPERATOR = "operator";
    private static final String CFG_STATE = "state";

    @SuppressWarnings("rawtypes")
    private ServiceRegistration eventSubscriberRegistration;

    private ItemRegistry itemRegistry;

    public AboveBelowTriggerHandler(Trigger module, BundleContext bundleContext) {
        super(module);
        this.itemName = (String) module.getConfiguration().get(CFG_ITEMNAME);
        this.state = (String) module.getConfiguration().get(CFG_STATE);
        this.operator = (String) module.getConfiguration().get(CFG_OPERATOR);
        HashSet<String> set = new HashSet<>();
        set.add(ItemStateChangedEvent.TYPE);
        set.add(GroupItemStateChangedEvent.TYPE);
        this.types = Collections.unmodifiableSet(set);
        this.bundleContext = bundleContext;
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("event.topics", "smarthome/items/" + itemName + "/*");
        eventSubscriberRegistration = this.bundleContext.registerService(EventSubscriber.class.getName(), this,
                properties);
    }

    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return types;
    }

    @Override
    public EventFilter getEventFilter() {
        return this;
    }

    @Override
    public void receive(Event event) {
        if (itemRegistry == null) {
            logger.error("The ItemRegistry is not available to evaluate the condition.");
            return;
        }
        if (operator == null || state == null || itemName == null) {
            logger.error("Module is not well configured: itemName={}  operator={}  state = {}", itemName, operator,
                    state);
            return;
        }
        if (ruleEngineCallback != null) {
            logger.trace("Received Event: Source: {} Topic: {} Type: {}  Payload: {}", event.getSource(),
                    event.getTopic(), event.getType(), event.getPayload());
            Map<String, Object> values = new HashMap<>();
            if (event instanceof ItemStateChangedEvent && ABOVE_BELOW_TYPE_ID.equals(module.getTypeUID())) {
                State itemState = ((ItemStateChangedEvent) event).getItemState();
                State oldState = ((ItemStateChangedEvent) event).getOldItemState();
                if (itemState == null || itemState instanceof UnDefType || oldState == null
                        || oldState instanceof UnDefType) {
                    logger.error("State or oldState null: state={} oldState={}", itemState, oldState);
                    return;
                }

                Item item;
                try {
                    item = itemRegistry.getItem(itemName);
                } catch (ItemNotFoundException e) {
                    logger.error("Item with Name {} not found in itemRegistry", itemName);
                    return;
                }
                State compareState = TypeParser.parseState(item.getAcceptedDataTypes(), this.state);
                switch (operator) {
                    case "=":
                        logger.debug("ConditionSatisfied --> {}", itemState.equals(compareState));
                        if (itemState.equals(compareState) && !oldState.equals(compareState)) {
                            values.put("state", itemState);
                        }
                        break;
                    case "!=":
                        if (!itemState.equals(compareState) && oldState.equals(compareState)) {
                            values.put("state", itemState);
                        }
                        break;
                    case "<":
                        if (itemState instanceof DecimalType && compareState instanceof DecimalType) {
                            if (((DecimalType) itemState).compareTo((DecimalType) compareState) < 0
                                    && ((DecimalType) oldState).compareTo((DecimalType) compareState) >= 0) {
                                values.put("state", itemState);
                            }
                        }
                        break;
                    case "<=":
                    case "=<":
                        if (itemState instanceof DecimalType && compareState instanceof DecimalType) {
                            if (((DecimalType) itemState).compareTo((DecimalType) compareState) <= 0
                                    && ((DecimalType) oldState).compareTo((DecimalType) compareState) > 0) {
                                values.put("state", itemState);
                            }
                        }
                        break;
                    case ">":
                        if (itemState instanceof DecimalType && compareState instanceof DecimalType) {
                            if (((DecimalType) itemState).compareTo((DecimalType) compareState) > 0
                                    && ((DecimalType) oldState).compareTo((DecimalType) compareState) <= 0) {
                                values.put("state", itemState);
                            }
                        }
                        break;
                    case ">=":
                    case "=>":
                        if (itemState instanceof DecimalType && compareState instanceof DecimalType) {
                            if (((DecimalType) itemState).compareTo((DecimalType) compareState) >= 0
                                    && ((DecimalType) oldState).compareTo((DecimalType) compareState) < 0) {
                                values.put("state", itemState);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
            if (!values.isEmpty()) {
                values.put("event", event);
                ruleEngineCallback.triggered(this.module, values);
            }
        }
    }

    /**
     * do the cleanup: unregistering eventSubscriber...
     */
    @Override
    public void dispose() {
        super.dispose();
        if (eventSubscriberRegistration != null) {
            eventSubscriberRegistration.unregister();
            eventSubscriberRegistration = null;
        }
    }

    @Override
    public boolean apply(Event event) {
        logger.trace("->FILTER: {}:{}", event.getTopic(), itemName);
        return event.getTopic().contains("/" + itemName + "/");
    }

}
