/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.core.factory;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.module.core.handler.ItemStateTriggerHandler;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This HandlerFactory creates ModuleHandlers to control items within the
 * RuleEngine. It contains basic Triggers, Conditions and Actions.
 *
 * @author Benedikt Niehues - Initial contribution and API
 * @author Kai Kreuzer - refactored and simplified customized module handling
 *
 */
public class CoreModuleHandlerFactory extends BaseModuleHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(CoreModuleHandlerFactory.class);

    private static final Collection<String> types = Arrays.asList(ItemStateTriggerHandler.ABOVE_BELOW_TYPE_ID);

    private ItemRegistry itemRegistry;

    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext.getBundleContext());
    }

    protected void deactivate(ComponentContext componentContext) {
        super.deactivate();
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
        for (ModuleHandler handler : getHandlers().values()) {
            if (handler instanceof ItemStateTriggerHandler) {
                ((ItemStateTriggerHandler) handler).setItemRegistry(this.itemRegistry);
            }
        }
    }

    /**
     * unsetter for itemRegistry (called by serviceTracker)
     *
     * @param itemRegistry
     */
    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        for (ModuleHandler handler : getHandlers().values()) {
            if (handler instanceof ItemStateTriggerHandler) {
                ((ItemStateTriggerHandler) handler).unsetItemRegistry(this.itemRegistry);
            }
        }
        this.itemRegistry = null;
    }

    @Override
    public Collection<String> getTypes() {
        return types;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    protected synchronized ModuleHandler internalCreate(final Module module, final String ruleUID) {
        logger.trace("create {} -> {} : {}", module.getId(), module.getTypeUID(), ruleUID);
        final String moduleTypeUID = module.getTypeUID();
        if (module instanceof Trigger) {
            if (ItemStateTriggerHandler.ABOVE_BELOW_TYPE_ID.equals(moduleTypeUID)) {
                ItemStateTriggerHandler handler = new ItemStateTriggerHandler((Trigger) module, this.bundleContext);
                handler.setItemRegistry(itemRegistry);
                return handler;
            }
        }

        logger.error("The ModuleHandler is not supported:{}", moduleTypeUID);
        return null;
    }
}
