/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.northq.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.northq.discovery.NorthQDiscovery;
import org.openhab.binding.northq.handler.NorthQBindingHandler;
import org.openhab.binding.northq.handler.NorthQBridgeHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link NorthQBindingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Kaare Joergensen - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.northqbinding")
public class NorthQBindingHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(NorthQBridgeHandler.SUPPORTED_THING_TYPES.stream(),
                    NorthQBindingHandler.SUPPORTED_THING_TYPES.stream())
            .collect(Collectors.toSet());
    private Map<ThingUID, ServiceRegistration<?>> serviceRegistrations = new HashMap<>();

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (NorthQBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        } else if (NorthQBindingHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        }
        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the NorthQ binding");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (NorthQBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            NorthQBridgeHandler bridgeHandler = new NorthQBridgeHandler((Bridge) thing);
            registerDiscoveryService(bridgeHandler);
            return bridgeHandler;
        } else if (NorthQBindingHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new NorthQBindingHandler(thing);
        }

        return null;
    }

    private synchronized void registerDiscoveryService(NorthQBridgeHandler bridgeHandler) {
        NorthQDiscovery discoveryService = new NorthQDiscovery(bridgeHandler);
        discoveryService.activate();
        serviceRegistrations.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    protected void removeHandler(@NonNull ThingHandler thingHandler) {
        if (thingHandler instanceof NorthQBridgeHandler) {
            ServiceRegistration<?> registration = serviceRegistrations.get(thingHandler.getThing().getUID());
            if (registration != null) {
                NorthQDiscovery discovery = (NorthQDiscovery) bundleContext.getService(registration.getReference());
                if (discovery != null) {
                    discovery.deactivate();
                }
                registration.unregister();
                serviceRegistrations.remove(thingHandler.getThing().getUID());
            }
        }
        super.removeHandler(thingHandler);
    }
}
