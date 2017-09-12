package org.openhab.binding.northqbinding.handler;

import static org.openhab.binding.northqbinding.NorthQBindingBindingConstants.*;

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
import org.json.JSONException;
import org.openhab.binding.northqbinding.network.APIManager;
import org.openhab.binding.northqbinding.network.NetworkErrorException;;

public class NorthQBridgeHandler extends ConfigStatusBridgeHandler {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);
    private APIManager apimanager;

    public NorthQBridgeHandler(Bridge bridge) {
        super(bridge);
        // TODO Auto-generated constructor stub
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
        apimanager = new APIManager();
        if (getConfig().get(USER_NAME) != null && getConfig().get(PASSWORD) != null) {

            try {
                apimanager.authenticate((String) getConfig().get(USER_NAME), (String) getConfig().get(PASSWORD));
            } catch (NetworkErrorException | JSONException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR);
            }

        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-ip-address");
        }

    }

}
