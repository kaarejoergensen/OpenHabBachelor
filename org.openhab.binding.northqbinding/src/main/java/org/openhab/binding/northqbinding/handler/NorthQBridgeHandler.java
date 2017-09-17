package org.openhab.binding.northqbinding.handler;

import static org.openhab.binding.northqbinding.NorthQBindingBindingConstants.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
import org.openhab.binding.northqbinding.network.QStickBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;;

public class NorthQBridgeHandler extends ConfigStatusBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(NorthQBridgeHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);
    private QStickBridge qStickBridge;
    private Map<String, BinarySwitch> binarySwitches = new ConcurrentHashMap<>();
    private Map<String, BinarySensor> binarySensors = new ConcurrentHashMap<>();

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

    public BinarySwitch getBinarySwitchById(String node_id) {
        if (node_id == null || node_id.isEmpty()) {
            return null;
        }
        BinarySwitch binarySwitch = binarySwitches.get(node_id);
        if (binarySwitch == null) {
            getAllBinarySwitches();
            binarySwitch = binarySwitches.get(node_id);
        }
        return binarySwitch;
    }

    public List<BinarySwitch> getAllBinarySwitches() {
        if (qStickBridge != null) {
            try {
                List<BinarySwitch> binarySwitchs = qStickBridge.getAllSwitches();
                for (BinarySwitch b : binarySwitchs) {
                    binarySwitches.put(String.valueOf(b.getNode_id()), b);
                }
                return binarySwitchs;
            } catch (APIException | IOException e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyList();
    }

    public BinarySensor getBinarySensorById(String node_id) {
        if (node_id == null || node_id.isEmpty()) {
            return null;
        }
        BinarySensor binarySensor = binarySensors.get(node_id);
        if (binarySensors == null) {
            getAllBinarySensors();
            binarySensor = binarySensors.get(node_id);
        }
        return binarySensor;
    }

    public List<BinarySensor> getAllBinarySensors() {
        if (qStickBridge != null) {
            try {
                List<BinarySensor> binarySensorList = qStickBridge.getAllBinarySensors();
                for (BinarySensor b : binarySensorList) {
                    binarySensors.put(String.valueOf(b.getNode_id()), b);
                }
                return binarySensorList;
            } catch (APIException | IOException e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyList();
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

}
