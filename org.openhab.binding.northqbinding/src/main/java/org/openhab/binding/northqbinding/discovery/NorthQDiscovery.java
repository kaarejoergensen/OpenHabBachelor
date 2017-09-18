package org.openhab.binding.northqbinding.discovery;

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
import org.openhab.binding.northqbinding.models.BinarySensor;
import org.openhab.binding.northqbinding.models.BinarySwitch;
import org.openhab.binding.northqbinding.models.NorthQThing;

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
            if (thing instanceof BinarySwitch) {
                addBinarySwitch((BinarySwitch) thing);
            } else if (thing instanceof BinarySensor) {
                addBinarySensor((BinarySensor) thing);
            }
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        scheduler.schedule(() -> {
            startScan();
        }, 1, TimeUnit.SECONDS);
    }

    private void addBinarySwitch(BinarySwitch binarySwitch) {
        ThingUID thingUID = getThingUID(binarySwitch);
        ThingTypeUID thingTypeUID = NorthQBindingBindingConstants.BINARY_SWITCH;
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        Map<String, Object> properties = new HashMap<>(1);
        properties.put(NorthQBindingBindingConstants.NODE_ID, String.valueOf(binarySwitch.getNode_id()));
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withProperties(properties).withBridge(bridgeUID).withLabel(binarySwitch.getName()).build();
        thingDiscovered(discoveryResult);

    }

    private void addBinarySensor(BinarySensor binarySensor) {
        ThingUID thingUID = getBinarySensorUID(binarySensor);
        ThingTypeUID thingTypeUID = NorthQBindingBindingConstants.BINARY_SENSOR;
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        Map<String, Object> properties = new HashMap<>(1);
        properties.put(NorthQBindingBindingConstants.NODE_ID, String.valueOf(binarySensor.getNode_id()));
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withProperties(properties).withBridge(bridgeUID).withLabel(binarySensor.getName()).build();
        thingDiscovered(discoveryResult);

    }

    private ThingUID getThingUID(BinarySwitch binarySwitch) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingTypeUID thingTypeUID = NorthQBindingBindingConstants.BINARY_SWITCH;
        return new ThingUID(thingTypeUID, bridgeUID, String.valueOf(binarySwitch.getNode_id()));
    }

    private ThingUID getBinarySensorUID(BinarySensor binarySensor) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingTypeUID thingTypeUID = NorthQBindingBindingConstants.BINARY_SENSOR;
        return new ThingUID(thingTypeUID, bridgeUID, String.valueOf(binarySensor.getNode_id()));
    }
}
