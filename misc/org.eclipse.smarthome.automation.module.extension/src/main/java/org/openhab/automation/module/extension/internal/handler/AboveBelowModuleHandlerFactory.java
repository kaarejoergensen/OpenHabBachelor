/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.automation.module.extension.internal.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.openhab.automation.module.extension.internal.type.AboveBelowTriggerType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a simple implementation of the {@link ModuleHandlerFactory}, which is registered as a service.
 *
 * @author Kåre Jørgensen - Initial Contribution
 *
 */
public class AboveBelowModuleHandlerFactory extends BaseModuleHandlerFactory {

    public static final String MODULE_HANDLER_FACTORY_NAME = "[JavaAPIDemoHandlerFactory]";
    private static final Collection<String> TYPES;

    private ItemRegistry itemRegistry;

    static {
        List<String> temp = new ArrayList<String>();
        temp.add(AboveBelowTriggerType.UID);
        TYPES = Collections.unmodifiableCollection(temp);
    }

    @SuppressWarnings("rawtypes")
    private ServiceRegistration factoryRegistration;
    private Map<String, AboveBelowTriggerHandler> triggerHandlers;
    private Logger logger = LoggerFactory.getLogger(AboveBelowModuleHandlerFactory.class);

    public AboveBelowModuleHandlerFactory(BundleContext bc) {
        triggerHandlers = new HashMap<String, AboveBelowTriggerHandler>();
        activate(bc);
    }

    @Override
    public Collection<String> getTypes() {
        return TYPES;
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        logger.debug("Added itemRegistry={}", itemRegistry);
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

    public void register(BundleContext bc) {
        factoryRegistration = bc.registerService(ModuleHandlerFactory.class.getName(), this, null);
    }

    public void unregister() {
        factoryRegistration.unregister();
        factoryRegistration = null;
    }

    public AboveBelowTriggerHandler getTriggerHandler(String uid) {
        return triggerHandlers.get(uid);
    }

    @Override
    protected ModuleHandler internalCreate(Module module, String ruleUID) {
        AboveBelowTriggerHandler moduleHandler = null;
        if (AboveBelowTriggerType.UID.equals(module.getTypeUID())) {
            moduleHandler = new AboveBelowTriggerHandler((Trigger) module, this.bundleContext);
            moduleHandler.setItemRegistry(this.itemRegistry);
            triggerHandlers.put(ruleUID, moduleHandler);
        } else {
            logger.error(MODULE_HANDLER_FACTORY_NAME + "Not supported moduleHandler: {}", module.getTypeUID());
        }
        return moduleHandler;
    }
}
