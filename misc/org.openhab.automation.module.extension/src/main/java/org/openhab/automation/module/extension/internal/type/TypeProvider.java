/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.automation.module.extension.internal.type;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 *
 *
 * @author Kåre Jørgensen - Initial Contribution
 *
 */
public class TypeProvider implements ModuleTypeProvider {

    private Map<String, ModuleType> providedModuleTypes;
    @SuppressWarnings("rawtypes")
    private ServiceRegistration providerReg;

    public TypeProvider() {
        providedModuleTypes = new HashMap<String, ModuleType>();
        providedModuleTypes.put(AboveBelowTriggerType.UID, AboveBelowTriggerType.initialize());
        providedModuleTypes.put(BetweenTimesConditionType.UID, BetweenTimesConditionType.initialize());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModuleType> T getModuleType(String UID, Locale locale) {
        return (T) providedModuleTypes.get(UID);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModuleType> Collection<T> getModuleTypes(Locale locale) {
        return (Collection<T>) providedModuleTypes.values();
    }

    /**
     * To provide the {@link ModuleType}s should register the TypeProvider as
     * {@link ModuleTypeProvider} service.
     *
     * @param bc
     *            is a bundle's execution context within the Framework.
     */
    public void register(BundleContext bc) {
        providerReg = bc.registerService(ModuleTypeProvider.class.getName(), this, null);
    }

    /**
     * This method unregisters the TypeProvider as {@link ModuleTypeProvider}
     * service.
     */
    public void unregister() {
        providerReg.unregister();
        providerReg = null;
        providedModuleTypes = null;
    }

    @Override
    public void addProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
        // does nothing because this provider does not change
    }

    @Override
    public Collection<ModuleType> getAll() {
        return Collections.unmodifiableCollection(providedModuleTypes.values());
    }

    @Override
    public void removeProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
        // does nothing because this provider does not change
    }

}
