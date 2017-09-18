package org.openhab.binding.northqbinding.handler;

import static org.openhab.binding.northqbinding.NorthQBindingBindingConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.northqbinding.exceptions.APIException;
import org.openhab.binding.northqbinding.models.BinarySensor;
import org.openhab.binding.northqbinding.models.BinarySwitch;
import org.openhab.binding.northqbinding.models.NorthQThing;
import org.openhab.binding.northqbinding.network.QStickBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;;

public class NorthQBridgeHandler extends ConfigStatusBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(NorthQBridgeHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);
    private static final int REFRESH = 15;

    private QStickBridge qStickBridge;
    List<NorthQThing> things = new ArrayList<>();
    private Map<String, List<NorthQThing>> thingMap = new ConcurrentHashMap<>();

    private List<BindingHandlerInterface> handlers = new ArrayList<>();
    private ScheduledFuture<?> refreshJob;
    private volatile boolean running = false;
    private Runnable networkRunable = new Runnable() {
        @Override
        public void run() {
            running = true;
            logger.debug("Running NorthQ refresh");
            try {
                if (qStickBridge != null) {
                    List<String> gatewayStatuses = qStickBridge.getAllGatewayStatuses();
                    things = qStickBridge.getAllThings(gatewayStatuses);
                    thingMap = new ConcurrentHashMap<>();
                    for (NorthQThing thing : things) {
                        if (thingMap.containsKey(String.valueOf(thing.getNode_id()))) {
                            thingMap.get(String.valueOf(thing.getNode_id())).add(thing);
                        } else {
                            thingMap.put(String.valueOf(thing.getNode_id()), new ArrayList<>(Arrays.asList(thing)));
                        }
                    }
                    logger.debug("Notifying {} handlers of {} things", handlers.size(), things.size());
                    things.stream().forEach(t -> notifyHandlers(t));
                }
            } catch (APIException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                running = false;
            }
        }
    };

    public NorthQBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialize() {
        logger.debug("Initializing Q-Stick bridge");
        if (getConfig().get(USER_NAME) != null && getConfig().get(PASSWORD) != null) {
            try {
                qStickBridge = new QStickBridge((String) getConfig().get(USER_NAME),
                        (String) getConfig().get(PASSWORD));
                logger.debug("Logged in to NorthQ");
                updateStatus(ThingStatus.ONLINE);
                startAutomaticRefresh();
            } catch (APIException | IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR);
                logger.warn("Authentication error: " + e.getMessage());
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-ip-address");
            logger.warn("Configuration error. Lacking user or password.");
        }
    }

    private void startAutomaticRefresh() {
        if (qStickBridge != null) {
            refreshJob = scheduler.scheduleWithFixedDelay(networkRunable, 0, REFRESH, TimeUnit.SECONDS);
        }
    }

    public BinarySwitch getBinarySwitchById(String node_id) {
        if (node_id == null || node_id.isEmpty()) {
            return null;
        }
        List<NorthQThing> things = thingMap.get(node_id);
        if (things == null) {
            return null;
        }
        for (NorthQThing thing : things) {
            if (thing instanceof BinarySwitch) {
                return (BinarySwitch) thing;
            }
        }
        return null;
    }

    public BinarySensor getBinarySensorById(String node_id) {
        if (node_id == null || node_id.isEmpty()) {
            return null;
        }
        List<NorthQThing> things = thingMap.get(node_id);
        if (things == null) {
            return null;
        }
        for (NorthQThing thing : things) {
            if (thing instanceof BinarySensor) {
                return (BinarySensor) thing;
            }
        }
        return null;
    }

    public List<NorthQThing> getAllNorthQThings() {
        if (running) {
            do {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (running);
            return things;
        }
        networkRunable.run();
        return things;
    }

    public void changeSwitchState(BinarySwitch binarySwitch) throws IOException, APIException {
        qStickBridge.changeSwitchState(binarySwitch);
    }

    public void armSensor(BinarySensor binarySensor) throws IOException, APIException {
        qStickBridge.armSensor(binarySensor);
    }

    public void disArmSensor(BinarySensor binarySensor) throws IOException, APIException {
        qStickBridge.disArmSensor(binarySensor);
    }

    private void notifyHandlers(NorthQThing thing) {
        handlers.stream().forEach(h -> h.onThingStateChanged(thing));
    }

    public boolean addHandler(BindingHandlerInterface handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Null handler not allowed");
        }
        boolean result = handlers.add(handler);
        if (result) {
            getAllNorthQThings();
            for (NorthQThing thing : things) {
                handler.onThingStateChanged(thing);
            }
        }
        return result;
    }

    public boolean removeHandler(BindingHandlerInterface handler) {
        return handlers.remove(handler);
    }

}
