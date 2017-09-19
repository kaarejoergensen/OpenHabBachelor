package org.openhab.binding.northqbinding.discovery;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.northqbinding.NorthQBindingBindingConstants;
import org.openhab.binding.northqbinding.handler.NorthQBindingHandler;
import org.openhab.binding.northqbinding.handler.NorthQBridgeHandler;
import org.openhab.binding.northqbinding.models.NorthQThing;
import org.openhab.binding.northqbinding.models.Thermostat;

public class NorthQDiscovery extends AbstractDiscoveryService {
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;

    private NorthQBridgeHandler bridgeHandler;

    public NorthQDiscovery(NorthQBridgeHandler bridgeHandler) throws IllegalArgumentException {
        super(NorthQBindingHandler.SUPPORTED_THING_TYPES, DISCOVER_TIMEOUT_SECONDS);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void activate(Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    protected void modified(Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    protected void startScan() {
        List<NorthQThing> things = bridgeHandler.getAllNorthQThings();
        for (NorthQThing thing : things) {
            if (thingActive(thing)) {
                addNorthQThing(thing);
            }
        }
    }

    private boolean thingActive(NorthQThing thing) {
        Date lastRead = new Date(thing.getRead() * 1000L);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -24);
        Date yesterday = calendar.getTime();
        return yesterday.before(lastRead);
    }

    @Override
    protected void startBackgroundDiscovery() {
        scheduler.schedule(() -> {
            startScan();
        }, 1, TimeUnit.SECONDS);
    }

    private void addNorthQThing(NorthQThing thing) {
        ThingUID thingUID = getThingUID(thing);
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        Map<String, Object> properties = new HashMap<>(1);
        properties.put(NorthQBindingBindingConstants.NODE_ID, String.valueOf(thing.getNode_id()));
        properties.put(NorthQBindingBindingConstants.ROOM_ID, String.valueOf(thing.getRoom()));
        String name = thing instanceof Thermostat ? "Thermostat" : thing.getName();
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withProperties(properties).withBridge(bridgeUID).withLabel(name).build();
        thingDiscovered(discoveryResult);
    }

    private ThingUID getThingUID(NorthQThing thing) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        return new ThingUID(thingTypeUID, bridgeUID, String.valueOf(thing.getNode_id()));
    }
}
