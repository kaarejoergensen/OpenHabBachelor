/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.northqbinding.models;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 *
 * @author Kaare Joergensen - Initial contribution
 */
public abstract class NorthQThing {
    protected int node_id;
    protected int room;
    protected String name;
    protected long read;
    protected long uploaded;
    protected String gateway;

    public int getNode_id() {
        return node_id;
    }

    public void setNode_id(int node_id) {
        this.node_id = node_id;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getRead() {
        return read;
    }

    public void setRead(long read) {
        this.read = read;
    }

    public long getUploaded() {
        return uploaded;
    }

    public void setUploaded(long uploaded) {
        this.uploaded = uploaded;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getUniqueId() {
        return String.format("%s%d%s", this.gateway, this.node_id, this.getThingTypeUID().getAsString());
    }

    public abstract ThingTypeUID getThingTypeUID();
}
