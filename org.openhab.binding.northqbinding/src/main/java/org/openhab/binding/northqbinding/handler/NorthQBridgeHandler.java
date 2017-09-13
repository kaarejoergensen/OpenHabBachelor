package org.openhab.binding.northqbinding.handler;

import static org.openhab.binding.northqbinding.NorthQBindingBindingConstants.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

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
import org.openhab.binding.northqbinding.network.QStickBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;;

public class NorthQBridgeHandler extends ConfigStatusBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(NorthQBridgeHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);
    private QStickBridge qStickBridge;

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

}
