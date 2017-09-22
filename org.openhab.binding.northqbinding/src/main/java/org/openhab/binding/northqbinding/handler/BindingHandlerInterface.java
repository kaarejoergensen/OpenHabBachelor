package org.openhab.binding.northqbinding.handler;

import org.openhab.binding.northqbinding.models.NorthQThing;

public interface BindingHandlerInterface {

    void onThingStateChanged(NorthQThing thing);

    void onThingAdded(NorthQThing thing);

    void onThingRemoved(NorthQThing thing);

}
