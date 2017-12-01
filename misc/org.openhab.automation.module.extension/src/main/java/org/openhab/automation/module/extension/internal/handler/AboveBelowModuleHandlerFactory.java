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

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.openhab.automation.module.extension.internal.type.AboveBelowTriggerType;
import org.openhab.automation.module.extension.internal.type.BetweenTimesConditionType;
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

    public static final String MODULE_HANDLER_FACTORY_NAME = "[AboveBelowModuleHandlerFactory]";
    private static final Collection<String> TYPES;

    static {
        List<String> temp = new ArrayList<String>();
        temp.add(AboveBelowTriggerType.UID);
        temp.add(BetweenTimesConditionType.UID);
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
        ModuleHandler moduleHandler = null;
        if (AboveBelowTriggerType.UID.equals(module.getTypeUID())) {
            moduleHandler = new AboveBelowTriggerHandler((Trigger) module, this.bundleContext);
            triggerHandlers.put(ruleUID, (AboveBelowTriggerHandler) moduleHandler);
        } else if (BetweenTimesConditionType.UID.equals(module.getTypeUID())) {
            moduleHandler = new BetweenTimesConditionHandler((Condition) module);
        } else {
            logger.error(MODULE_HANDLER_FACTORY_NAME + "Not supported moduleHandler: {}", module.getTypeUID());
        }
        return moduleHandler;
    }
}
