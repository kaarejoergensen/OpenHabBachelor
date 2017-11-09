/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.automation.module.extension.factory;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.openhab.automation.module.extension.handler.AboveBelowTriggerHandler;
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
public class AboveBelowModuleHandlerFactory extends BaseModuleHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(AboveBelowModuleHandlerFactory.class);

    private static final Collection<String> types = Arrays.asList(AboveBelowTriggerHandler.ABOVE_BELOW_TYPE_ID);

    private ItemRegistry itemRegistry;

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
        for (ModuleHandler handler : getHandlers().values()) {
            if (handler instanceof AboveBelowTriggerHandler) {
                ((AboveBelowTriggerHandler) handler).setItemRegistry(this.itemRegistry);
            }
        }
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        for (ModuleHandler handler : getHandlers().values()) {
            if (handler instanceof AboveBelowTriggerHandler) {
                ((AboveBelowTriggerHandler) handler).unsetItemRegistry(this.itemRegistry);
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
            if (AboveBelowTriggerHandler.ABOVE_BELOW_TYPE_ID.equals(moduleTypeUID)) {
                AboveBelowTriggerHandler handler = new AboveBelowTriggerHandler((Trigger) module, this.bundleContext);
                handler.setItemRegistry(itemRegistry);
                return handler;
            }
        }

        logger.error("The ModuleHandler is not supported:{}", moduleTypeUID);
        return null;
    }
}
